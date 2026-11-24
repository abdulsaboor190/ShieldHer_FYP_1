"""
scripts/export_risk_model.py
----------------------------
Task R-2: Export the frozen Stage 2 model to a little-endian binary file,
generate a synthetic golden test set, and prove in Python that the export
reproduces scikit-learn.

Layout of account_risk_model_v1.bin (LITTLE-ENDIAN):
  - magic 4 ASCII bytes: "SHRM"
  - format version int32: 1
  - nFeatures int32: 5
  - hasScaler int32: 0 (or 1)
  - [if hasScaler: mean float64[nFeatures], scale float64[nFeatures]]
  - nTrees int32: 300
  - plattSlope float64
  - plattIntercept float64
  - clipLow float64
  - clipHigh float64
  - tau float64
  - For each tree:
      - nodeCount int32
      - left int32[nodeCount]
      - right int32[nodeCount]
      - feature int32[nodeCount]
      - threshold float64[nodeCount]
      - leafProb float64[nodeCount]
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import os
import pathlib
import struct
import sys
from typing import Any

import joblib
import numpy as np
import pandas as pd


def compute_sha256(path: pathlib.Path | str) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()


def compute_bytes_sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def load_ml_bundle(ml_root: pathlib.Path) -> tuple[Any, dict[str, Any], dict[str, Any], dict[str, Any]]:
    models_dir = ml_root / "models"
    config_path = models_dir / "config.json"
    calibrator_path = models_dir / "calibrator.json"
    abstention_path = models_dir / "abstention.json"
    manifest_path = models_dir / "freeze_manifest.json"
    rf_path = models_dir / "rf_pipeline.joblib"

    for p in (config_path, calibrator_path, abstention_path, manifest_path, rf_path):
        if not p.is_file():
            raise FileNotFoundError(f"Required ML file not found: {p}")

    with open(config_path, "r", encoding="utf-8") as f:
        config = json.load(f)
    with open(calibrator_path, "r", encoding="utf-8") as f:
        calibrator = json.load(f)
    with open(abstention_path, "r", encoding="utf-8") as f:
        abstention = json.load(f)
    with open(manifest_path, "r", encoding="utf-8") as f:
        freeze_manifest = json.load(f)

    pipeline = joblib.load(rf_path)
    return pipeline, config, calibrator, abstention, freeze_manifest


def validate_and_extract_pipeline(pipeline: Any) -> tuple[int, np.ndarray | None, np.ndarray | None, Any]:
    """Validate pipeline structure: [optional StandardScaler, RandomForestClassifier]."""
    steps = getattr(pipeline, "steps", None)
    if not steps:
        raise ValueError(f"Expected a scikit-learn Pipeline, got {type(pipeline)}")

    has_scaler = 0
    scaler_mean: np.ndarray | None = None
    scaler_scale: np.ndarray | None = None
    rf_model: Any = None

    if len(steps) == 1:
        name, step = steps[0]
        if step.__class__.__name__ != "RandomForestClassifier":
            raise ValueError(f"Expected single step to be RandomForestClassifier, got {step.__class__.__name__}")
        rf_model = step
    elif len(steps) == 2:
        name0, step0 = steps[0]
        name1, step1 = steps[1]
        if step0.__class__.__name__ != "StandardScaler":
            raise ValueError(f"Step 0 must be StandardScaler if 2 steps, got {step0.__class__.__name__}")
        if step1.__class__.__name__ != "RandomForestClassifier":
            raise ValueError(f"Step 1 must be RandomForestClassifier, got {step1.__class__.__name__}")
        has_scaler = 1
        scaler_mean = np.asarray(step0.mean_, dtype=np.float64)
        scaler_scale = np.asarray(step0.scale_, dtype=np.float64)
        rf_model = step1
    else:
        step_names = [s[0] for s in steps]
        raise ValueError(f"Unsupported pipeline steps: {step_names}. Only [StandardScaler?, RandomForestClassifier] allowed.")

    return has_scaler, scaler_mean, scaler_scale, rf_model


def build_binary(
    has_scaler: int,
    scaler_mean: np.ndarray | None,
    scaler_scale: np.ndarray | None,
    rf_model: Any,
    calibrator: dict[str, Any],
    abstention: dict[str, Any],
) -> bytes:
    """Build the little-endian binary bytes representing the model."""
    buf = bytearray()

    # Header
    buf.extend(b"SHRM")  # magic 4 ASCII bytes
    n_features = int(rf_model.n_features_in_)
    n_trees = int(rf_model.n_estimators)
    buf.extend(struct.pack("<iii", 1, n_features, has_scaler))

    if has_scaler:
        assert scaler_mean is not None and scaler_scale is not None
        for m in scaler_mean:
            buf.extend(struct.pack("<d", float(m)))
        for s in scaler_scale:
            buf.extend(struct.pack("<d", float(s)))

    platt_slope = float(calibrator["slope"])
    platt_intercept = float(calibrator["intercept"])
    clip_low = float(calibrator.get("clip_low", 0.001))
    clip_high = float(calibrator.get("clip_high", 0.999))
    tau = float(abstention["tau"])

    buf.extend(struct.pack("<iddddd", n_trees, platt_slope, platt_intercept, clip_low, clip_high, tau))

    # Trees
    class_idx_1 = 1  # class 1 is risk
    for estimator in rf_model.estimators_:
        tree = estimator.tree_
        node_count = int(tree.node_count)
        buf.extend(struct.pack("<i", node_count))

        children_left = tree.children_left.astype(np.int32)
        children_right = tree.children_right.astype(np.int32)
        feature = tree.feature.astype(np.int32)
        threshold = tree.threshold.astype(np.float64)

        # value shape is (node_count, 1, n_classes)
        vals = tree.value[:, 0, :]
        val_sum = np.sum(vals, axis=1)
        val_sum[val_sum == 0.0] = 1.0  # safe division
        leaf_prob = (vals[:, class_idx_1] / val_sum).astype(np.float64)

        buf.extend(children_left.tobytes())
        buf.extend(children_right.tobytes())
        buf.extend(feature.tobytes())
        buf.extend(threshold.tobytes())
        buf.extend(leaf_prob.tobytes())

    return bytes(buf)


class BinaryModelEvaluator:
    """Evaluator that reads from parsed binary arrays only (pure numpy)."""

    def __init__(self, data: bytes):
        offset = 0
        magic = data[offset : offset + 4]
        offset += 4
        if magic != b"SHRM":
            raise ValueError(f"Invalid magic: {magic}")

        version, n_features, has_scaler = struct.unpack_from("<iii", data, offset)
        offset += 12
        if version != 1:
            raise ValueError(f"Unsupported version: {version}")

        self.n_features = n_features
        self.has_scaler = bool(has_scaler)
        if self.has_scaler:
            self.scaler_mean = np.frombuffer(data, dtype=np.float64, count=n_features, offset=offset)
            offset += 8 * n_features
            self.scaler_scale = np.frombuffer(data, dtype=np.float64, count=n_features, offset=offset)
            offset += 8 * n_features
        else:
            self.scaler_mean = None
            self.scaler_scale = None

        n_trees, self.slope, self.intercept, self.clip_low, self.clip_high, self.tau = struct.unpack_from(
            "<iddddd", data, offset
        )
        offset += 4 + 8 * 5
        self.n_trees = n_trees

        self.trees = []
        for _ in range(n_trees):
            (node_count,) = struct.unpack_from("<i", data, offset)
            offset += 4

            left = np.frombuffer(data, dtype=np.int32, count=node_count, offset=offset)
            offset += 4 * node_count
            right = np.frombuffer(data, dtype=np.int32, count=node_count, offset=offset)
            offset += 4 * node_count
            feat = np.frombuffer(data, dtype=np.int32, count=node_count, offset=offset)
            offset += 4 * node_count
            thresh = np.frombuffer(data, dtype=np.float64, count=node_count, offset=offset)
            offset += 8 * node_count
            prob = np.frombuffer(data, dtype=np.float64, count=node_count, offset=offset)
            offset += 8 * node_count

            self.trees.append((node_count, left, right, feat, thresh, prob))

        if offset != len(data):
            raise ValueError(f"Extra trailing bytes: expected {offset}, got {len(data)}")

    def score_row(self, feats: np.ndarray) -> tuple[float, float, str, str]:
        """Score a single feature vector of 5 features (raw transformed)."""
        x = np.asarray(feats, dtype=np.float64).copy()
        if self.has_scaler:
            assert self.scaler_mean is not None and self.scaler_scale is not None
            x = (x - self.scaler_mean) / self.scaler_scale

        # Crucial: Cast to float32 then back to double for comparisons (scikit-learn tree check)
        x_f32 = x.astype(np.float32)

        leaf_probs = []
        for node_count, left, right, feat, thresh, prob in self.trees:
            node = 0
            while left[node] != -1:
                f_idx = feat[node]
                # Compare float32 feature value against float64 threshold
                if float(x_f32[f_idx]) <= thresh[node]:
                    node = left[node]
                else:
                    node = right[node]
            leaf_probs.append(prob[node])

        p_raw = float(np.mean(leaf_probs))

        # Platt calibration
        p_clipped = max(self.clip_low, min(self.clip_high, p_raw))
        x_logit = math.log(p_clipped / (1.0 - p_clipped))
        z = self.slope * x_logit + self.intercept
        p_cal = 1.0 / (1.0 + math.exp(-z))
        # Ensure inside (0, 1)
        p_cal = float(np.clip(p_cal, np.nextafter(0.0, 1.0), np.nextafter(1.0, 0.0)))

        # Confidence and abstention
        confidence = max(p_cal, 1.0 - p_cal)
        if confidence >= self.tau:
            state = "supported_prediction"
        else:
            state = "uncertain_prediction"

        label = "risk" if p_cal >= 0.5 else "genuine"
        return p_raw, p_cal, state, label


def generate_golden_set(ml_root: pathlib.Path, rf_model: Any, calibrator: dict[str, Any]) -> list[dict[str, Any]]:
    """Generate 700+ synthetic rows across random, boundary, edge, unsupported."""
    rng = np.random.default_rng(seed=42)
    rows: list[dict[str, Any]] = []
    row_id = 0

    # 1. 350 "random": log-uniform over 0 to 10M, desc 0 to 150, url 0 or 1
    for _ in range(350):
        row_id += 1
        # log-uniform: 10^u - 1 where u in [0, 7]
        followers = int(math.floor(math.exp(rng.uniform(0, math.log(10_000_001))) - 1))
        following = int(math.floor(math.exp(rng.uniform(0, math.log(10_000_001))) - 1))
        desc_len = int(rng.integers(0, 151))
        ext_url = int(rng.integers(0, 2))
        rows.append(
            {
                "id": f"gold_{row_id:04d}",
                "category": "random",
                "followers": followers,
                "following": following,
                "description_length": desc_len,
                "external_url": ext_url,
            }
        )

    # 2. 200 "boundary": randomly choose 200 tree nodes, invert threshold to count
    # Features: 0: followers, 1: following, 4: ratio
    candidate_nodes = []
    for t_idx, estimator in enumerate(rf_model.estimators_):
        tree = estimator.tree_
        for n_idx in range(tree.node_count):
            if tree.children_left[n_idx] != -1:  # split node
                f = tree.feature[n_idx]
                if f in (0, 1, 4):
                    candidate_nodes.append((f, tree.threshold[n_idx]))

    chosen_indices = rng.choice(len(candidate_nodes), size=200, replace=False)
    for idx in chosen_indices:
        f, thresh = candidate_nodes[idx]
        # Invert transform:
        # For f=0 (followers): thresh = log1p(count) => count = expm1(thresh)
        # For f=1 (following): thresh = log1p(count) => count = expm1(thresh)
        # For f=4 (ratio): thresh = log1p(ratio) => ratio = expm1(thresh) => followers = ratio * (following + 1)
        val = math.expm1(max(0.0, thresh))
        center_count = int(round(val))
        for offset in (-1, 0, 1):
            row_id += 1
            cnt = max(0, center_count + offset)
            if f == 0:
                fw, fg = cnt, 150
            elif f == 1:
                fw, fg = 300, cnt
            else:  # ratio
                fg = 100
                fw = max(0, int(round(val * (fg + 1.0)))) + offset
                fw = max(0, fw)

            rows.append(
                {
                    "id": f"gold_{row_id:04d}",
                    "category": "boundary",
                    "followers": fw,
                    "following": fg,
                    "description_length": 50,
                    "external_url": 0,
                }
            )

    # 3. 100 "edge": zeros, ones, very large counts (e.g. 2^40), desc 0, 1, 150, url combinations
    edge_counts = [0, 1, 2, 10, 100, 1000, 1_000_000, 100_000_000, 2**40]
    edge_descs = [0, 1, 150]
    edge_urls = [0, 1]

    edge_combos = []
    for fw in [0, 1, 2**40]:
        for fg in [0, 1, 2**40]:
            for d in edge_descs:
                for u in edge_urls:
                    edge_combos.append((fw, fg, d, u))

    # Add more diverse edges to reach at least 100
    for fw in edge_counts:
        for u in edge_urls:
            edge_combos.append((fw, fw, 0, u))
            edge_combos.append((fw, 0, 150, u))

    for fw, fg, d, u in edge_combos[:100]:
        row_id += 1
        rows.append(
            {
                "id": f"gold_{row_id:04d}",
                "category": "edge",
                "followers": fw,
                "following": fg,
                "description_length": d,
                "external_url": u,
            }
        )

    # 4. 50 "unsupported": negative counts, missing (None) followers or following
    unsupported_specs = [
        (-1, 100, 10, 0),
        (100, -1, 10, 0),
        (-5, -5, 0, 0),
        (None, 100, 10, 0),
        (100, None, 10, 0),
        (None, None, 0, 0),
        (-100, None, 50, 1),
        (None, -50, 50, 1),
    ]
    # Repeat/vary to reach 50
    for i in range(50):
        row_id += 1
        spec = unsupported_specs[i % len(unsupported_specs)]
        fw, fg, d, u = spec
        if fw is not None and fw < 0:
            fw = -(i + 1)
        if fg is not None and fg < 0:
            fg = -(i + 1)
        rows.append(
            {
                "id": f"gold_{row_id:04d}",
                "category": "unsupported",
                "followers": fw,
                "following": fg,
                "description_length": d,
                "external_url": u,
            }
        )

    return rows


def compute_official_predictions(
    ml_root: pathlib.Path,
    rows: list[dict[str, Any]],
) -> list[dict[str, Any]]:
    """Compute official predict_batch outputs from ML folder."""
    sys.path.insert(0, str((ml_root / "src").resolve()))
    from account_risk.predict import predict_batch

    df_in = pd.DataFrame(rows)
    # Call official predict_batch
    pred_df = predict_batch(df_in)

    res_rows = []
    for r, (_, p_row) in zip(rows, pred_df.iterrows()):
        state = p_row["state"]
        p_raw_val = p_row.get("p_rf")
        # In score_frame, columns are p_rf, p_cal, state, etc.
        # But predict_batch returns: state, risk_probability, confidence, reason, missing_features, invalid_features
        # Let's check heldout_eval.score_frame directly to get p_rf and p_cal
        res_rows.append(
            {
                **r,
                "state": state,
                "risk_probability": p_row["risk_probability"],
                "confidence": p_row["confidence"],
            }
        )
    return res_rows


def score_official_frame(
    ml_root: pathlib.Path,
    rows: list[dict[str, Any]],
    bundle: dict[str, Any],
) -> pd.DataFrame:
    """Run heldout_eval.score_frame to get official p_rf, p_cal, state."""
    sys.path.insert(0, str((ml_root / "src").resolve()))
    from account_risk.heldout_eval import score_frame

    df_in = pd.DataFrame(rows)
    scored = score_frame(bundle, df_in)
    return scored


def main() -> None:
    parser = argparse.ArgumentParser(description="Export frozen Stage 2 model to binary and create golden test set.")
    parser.add_argument("--ml-root", type=pathlib.Path, required=True, help="Path to frozen ml directory")
    parser.add_argument("--check", action="store_true", help="Verify in-memory generation matches committed files")
    args = parser.parse_args()

    ml_root = args.ml_root.resolve()
    repo_root = pathlib.Path(__file__).resolve().parent.parent

    # File paths
    bin_path = repo_root / "app" / "src" / "main" / "assets" / "account_risk_model_v1.bin"
    manifest_path = repo_root / "docs" / "stage3" / "account_risk_model_v1.manifest.json"
    csv_path = repo_root / "app" / "src" / "test" / "resources" / "account_risk_golden_v1.csv"

    # Load frozen bundle
    sys.path.insert(0, str((ml_root / "src").resolve()))
    from account_risk.bundle import load_bundle

    bundle = load_bundle(ml_root / "models", check_versions=True)
    pipeline = bundle["rf"]
    config = bundle["config"]
    calibrator = bundle["calibrator"]
    abstention = bundle["abstention"]

    # 1. Validate pipeline
    has_scaler, scaler_mean, scaler_scale, rf_model = validate_and_extract_pipeline(pipeline)

    # 2. Build binary in memory
    bin_bytes = build_binary(has_scaler, scaler_mean, scaler_scale, rf_model, calibrator, abstention)
    bin_sha256 = compute_bytes_sha256(bin_bytes)

    # 3. Create Evaluator from binary bytes for Python self-check
    evaluator = BinaryModelEvaluator(bin_bytes)

    # 4. Generate golden test set
    raw_rows = generate_golden_set(ml_root, rf_model, calibrator)
    scored_official = score_official_frame(ml_root, raw_rows, bundle)

    golden_rows = []
    max_diff_raw = 0.0
    max_diff_cal = 0.0

    # Self-check evaluator against official predictions on all rows
    for r, (_, off) in zip(raw_rows, scored_official.iterrows()):
        input_state = off["input_state"]
        off_state = off["state"]
        off_p_rf = off["p_rf"]
        off_p_cal = off["p_cal"]

        if input_state == "unsupported_input":
            expected_p_raw_str = ""
            expected_p_cal_str = ""
            expected_state = "unsupported_input"
            expected_label = ""
        else:
            expected_p_raw = float(off_p_rf)
            expected_p_cal = float(off_p_cal)
            expected_p_raw_str = f"{expected_p_raw:.16f}"
            expected_p_cal_str = f"{expected_p_cal:.16f}"
            expected_state = off_state
            expected_label = "risk" if expected_p_cal >= 0.5 else "genuine"

            # Compute features for evaluator self-check
            fw = float(r["followers"])
            fg = float(r["following"])
            ratio = fw / (fg + 1.0)
            feats = np.array(
                [
                    math.log1p(fw),
                    math.log1p(fg),
                    float(r["description_length"]),
                    float(r["external_url"]),
                    math.log1p(ratio),
                ],
                dtype=np.float64,
            )

            eval_p_raw, eval_p_cal, eval_state, eval_label = evaluator.score_row(feats)

            diff_raw = abs(eval_p_raw - expected_p_raw)
            diff_cal = abs(eval_p_cal - expected_p_cal)
            if diff_raw > max_diff_raw:
                max_diff_raw = diff_raw
            if diff_cal > max_diff_cal:
                max_diff_cal = diff_cal

            if diff_raw > 1e-12 or diff_cal > 1e-12:
                raise ValueError(
                    f"Self-check tolerance failure on row {r['id']} ({r['category']}): "
                    f"raw diff={diff_raw}, cal diff={diff_cal}"
                )
            if eval_state != expected_state:
                raise ValueError(
                    f"Self-check state mismatch on row {r['id']}: eval='{eval_state}' vs official='{expected_state}'"
                )
            if eval_label != expected_label:
                raise ValueError(
                    f"Self-check label mismatch on row {r['id']}: eval='{eval_label}' vs official='{expected_label}'"
                )

        fw_out = "" if r["followers"] is None else str(r["followers"])
        fg_out = "" if r["following"] is None else str(r["following"])
        golden_rows.append(
            {
                "id": r["id"],
                "category": r["category"],
                "followers": fw_out,
                "following": fg_out,
                "description_length": r["description_length"],
                "external_url": r["external_url"],
                "p_raw": expected_p_raw_str,
                "p_cal": expected_p_cal_str,
                "state": expected_state,
                "label": expected_label,
            }
        )

    # 5. Build manifest in memory
    total_nodes = sum(int(t.tree_.node_count) for t in rf_model.estimators_)
    manifest_dict = {
        "format_version": 1,
        "model_file": "account_risk_model_v1.bin",
        "sha256": bin_sha256,
        "freeze_manifest_sha256": "002b72b1593e3485f0f6bcdf0a3e450e77658e7d5766924224e8a0b4c232c514",
        "stage2_freeze_commit": "c014a1264a5648a48ad59713910ff1c694606f87",
        "library_versions": config.get("library_versions", {}),
        "n_trees": int(rf_model.n_estimators),
        "total_nodes": total_nodes,
        "feature_order": config.get("mask", {}).get("model_features", []),
        "has_scaler": has_scaler,
        "platt_constants": {
            "slope": float(calibrator["slope"]),
            "intercept": float(calibrator["intercept"]),
            "clip_low": float(calibrator.get("clip_low", 0.001)),
            "clip_high": float(calibrator.get("clip_high", 0.999)),
        },
        "tau": float(abstention["tau"]),
        "export_date": "2026-11-25",
        "note": "Exported from the frozen Stage 2 bundle; the ml folder was not modified.",
    }
    manifest_json_str = json.dumps(manifest_dict, indent=2) + "\n"

    # Build CSV in memory
    csv_df = pd.DataFrame(golden_rows)
    csv_str = csv_df.to_csv(index=False, lineterminator="\n")

    if args.check:
        # Check byte-for-byte against committed files
        if not bin_path.exists():
            raise FileNotFoundError(f"Missing {bin_path} during --check")
        if not manifest_path.exists():
            raise FileNotFoundError(f"Missing {manifest_path} during --check")
        if not csv_path.exists():
            raise FileNotFoundError(f"Missing {csv_path} during --check")

        committed_bin = bin_path.read_bytes()
        if committed_bin != bin_bytes:
            raise ValueError("account_risk_model_v1.bin does not match in-memory generation!")

        committed_manifest = manifest_path.read_text(encoding="utf-8")
        if committed_manifest.replace("\r\n", "\n") != manifest_json_str:
            raise ValueError("account_risk_model_v1.manifest.json does not match in-memory generation!")

        committed_csv = csv_path.read_text(encoding="utf-8")
        if committed_csv.replace("\r\n", "\n") != csv_str:
            raise ValueError("account_risk_golden_v1.csv does not match in-memory generation!")

        print("OK — all files byte-for-byte match in-memory export.")
        return

    # Write files
    bin_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    csv_path.parent.mkdir(parents=True, exist_ok=True)

    bin_path.write_bytes(bin_bytes)
    manifest_path.write_text(manifest_json_str, encoding="utf-8", newline="\n")
    csv_path.write_text(csv_str, encoding="utf-8", newline="\n")

    # Counts
    cat_counts = csv_df["category"].value_counts().to_dict()
    state_counts = csv_df["state"].value_counts().to_dict()

    print("=== EXPORT SUCCESSFUL ===")
    print(f"Model binary written: {bin_path} ({len(bin_bytes)} bytes)")
    print(f"Model binary SHA-256: {bin_sha256}")
    print(f"Manifest written: {manifest_path} ({compute_sha256(manifest_path)})")
    print(f"Golden CSV written: {csv_path} ({compute_sha256(csv_path)})")
    print(f"Self-check max difference raw: {max_diff_raw:.2e}, cal: {max_diff_cal:.2e}")
    print(f"Category counts: {cat_counts}")
    print(f"State counts: {state_counts}")
    print(f"Total golden rows: {len(golden_rows)}")


if __name__ == "__main__":
    main()
