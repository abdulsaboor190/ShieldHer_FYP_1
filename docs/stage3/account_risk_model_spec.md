# ShieldHer Stage 3: Account-Risk Model Specification (v1)

**Document:** `docs/stage3/account_risk_model_spec.md`  
**Status:** Frozen Stage 2 Model Specification for On-Device Kotlin Inference  
**Verification Target:** Reproduce exact inference behavior of `D:\23i-3039\P2\ShieldHer1\ml` without network or external libraries.  

Every statement in this specification is explicitly marked as **[VERIFIED IN CODE]** or **[INFERENCE]**.

---

## 1. Production Interface

### 1.1 `predict_one` Interface **[VERIFIED IN CODE: `ml/src/account_risk/predict.py`]**
Accepts a dictionary of raw account features:
- `features`: `dict[str, Any]`
  - `'followers'`: integer / numeric count, whole number $\ge 0$, or `None` / missing.
  - `'following'`: integer / numeric count, whole number $\ge 0$, or `None` / missing.
  - `'description_length'`: integer / numeric count $\ge 0$, or `None` / missing (empty bio).
  - `'external_url'`: integer / numeric $0$ or $1$, or `None` / missing (absence of URL).
- `bundle`: optional loaded bundle dict (defaults to frozen bundle cache).

Returns a dictionary with exact keys and types:
- `'state'`: `str` — one of `"supported_prediction"`, `"uncertain_prediction"`, `"unsupported_input"`.
- `'risk_probability'`: `float` in $[0.0, 1.0]$ when state is `"supported_prediction"` or `"uncertain_prediction"`; `None` when state is `"unsupported_input"`.
- `'confidence'`: `float` in $[0.5, 1.0]$ when state is `"supported_prediction"` or `"uncertain_prediction"`; `None` when state is `"unsupported_input"`.
- `'reason'`: `str` — empty string `""` for `"supported_prediction"`; explanation string for `"uncertain_prediction"` (e.g. `"confidence 0.65 is below the threshold 0.78"`) or `"unsupported_input"` (e.g. `"missing: followers"`).
- `'missing_features'`: `list[str]` — list of missing feature names (e.g. `['followers']`).
- `'invalid_features'`: `list[str]` — list of invalid feature names (e.g. `['following']` if negative).
- `'metadata'`: `dict[str, Any]` containing model version, freeze commit, manifest hash, feature list, calibration note, threshold, disclaimer.

### 1.2 `predict_batch` Interface **[VERIFIED IN CODE: `ml/src/account_risk/predict.py`]**
Accepts a `pandas.DataFrame` containing columns `followers`, `following`, `description_length`, `external_url` (missing columns filled with `NaN`; extra columns ignored).  
Returns a `pandas.DataFrame` with identical index and columns: `state`, `risk_probability`, `confidence`, `reason`, `missing_features`, `invalid_features`.

---

## 2. Features and Feature Order

### 2.1 Feature Set and Exact Order **[VERIFIED IN CODE: `ml/src/account_risk/feature_mask.py`, `ml/models/config.json`]**
The primary model (Mask A) uses exactly 5 features in this fixed canonical order ($D = 5$):
1. `followers` (index 0)
2. `following` (index 1)
3. `description_length` (index 2)
4. `external_url` (index 3)
5. `follower_following_ratio` (index 4)

### 2.2 Mathematical Formulas for Features **[VERIFIED IN CODE: `ml/src/account_risk/features.py`]**
Given raw valid inputs $N_{\text{followers}} \ge 0$, $N_{\text{following}} \ge 0$, $L_{\text{desc}} \ge 0$, $U_{\text{ext}} \in \{0, 1\}$:
1. **Feature 0 (`followers`):**
   $$\text{feature}[0] = \ln(1.0 + N_{\text{followers}}) \quad (\text{via } \texttt{np.log1p})$$
2. **Feature 1 (`following`):**
   $$\text{feature}[1] = \ln(1.0 + N_{\text{following}}) \quad (\text{via } \texttt{np.log1p})$$
3. **Feature 2 (`description_length`):**
   $$\text{feature}[2] = \text{float}(L_{\text{desc}})$$
4. **Feature 3 (`external_url`):**
   $$\text{feature}[3] = \text{float}(U_{\text{ext}})$$
5. **Feature 4 (`follower_following_ratio`):**
   $$\text{ratio} = \frac{N_{\text{followers}}}{N_{\text{following}} + 1.0}$$
   $$\text{feature}[4] = \ln(1.0 + \text{ratio}) \quad (\text{via } \texttt{np.log1p})$$

### 2.3 Text Length and URL Rules **[VERIFIED IN CODE: `ml/src/account_risk/features.py`, `predict.py`]**
- **Empty Bio / Missing Description:** If bio is empty or `None`, $L_{\text{desc}} = 0.0$.
- **Bio Length Unit:** In Python string handling, `len(str)` counts Unicode code points (characters). For Android/Kotlin equivalence, `bioText.codePointCount(0, bioText.length)` must be used rather than UTF-16 code units (`length()`) to match Python on emojis and multi-byte characters.
- **Empty External URL / Missing Link:** If external URL is absent or `None`, $U_{\text{ext}} = 0.0$. If present and non-empty, $U_{\text{ext}} = 1.0$.

---

## 3. Unsupported-Input Rules

### 3.1 Validation Rules **[VERIFIED IN CODE: `ml/src/account_risk/features.py` lines 58-85]**
- **Followers & Following:**
  - Must be present (non-null / non-NaN). Missing value $\rightarrow$ `"missing: followers"` or `"missing: following"`.
  - Must be finite and non-negative ($N \ge 0$). Negative value or infinity $\rightarrow$ `"invalid: followers"` or `"invalid: following"`.
  - Non-integer or non-numeric strings that cannot be parsed as numeric $\rightarrow$ `"missing"` / `"invalid"`.
  - Numbers with more than 15 digits (exceeding realistic profile counts or long capacity) are rejected by input parsers.
- **Description Length:**
  - `None` or `NaN` maps to $0.0$ (supported).
  - Negative values ($< 0$) or infinite values $\rightarrow$ `"invalid: description_length"`.
- **External URL:**
  - `None` or `NaN` maps to $0.0$ (supported).
  - Any value other than $0$ or $1$ $\rightarrow$ `"invalid: external_url"`.
- **Outcome:** Any row with a missing or invalid input has `state = "unsupported_input"`, `risk_probability = None`, `confidence = None`. No imputation is performed.

---

## 4. Pipeline Structure

### 4.1 Pipeline Steps **[VERIFIED IN CODE: `ml/models/rf_pipeline.joblib`]**
- Inspecting `rf_pipeline.joblib`:
  ```text
  Pipeline(steps=[('model', RandomForestClassifier(n_estimators=300, random_state=42))])
  ```
- **Scaler:** There is **NO StandardScaler or any scaler** in the pipeline. `hasScaler = 0`.
- **Classifier:** `sklearn.ensemble.RandomForestClassifier`.
- **Classes:** `classes_ = array([0, 1])` where Class 0 = "genuine", Class 1 = "risk".
- **Number of Estimators ($n_{\text{trees}}$):** `300`.
- **Total Nodes across all 300 Trees:** `32,770`.
- **Tree Leaf Representation in scikit-learn:**
  - Left child: `children_left[node]` ($-1$ indicates leaf).
  - Right child: `children_right[node]` ($-1$ indicates leaf).
  - Feature: `feature[node]` ($-2$ for leaf, otherwise $0 \dots 4$).
  - Threshold: `threshold[node]` stored as `float64` in `tree_.threshold`.
  - Leaf values: `tree_.value[node, 0, :]`. For this model, values at node 0 and leaves are stored as normalized fractions / probabilities summing to $1.0$ (e.g. `[[0.5, 0.5]]`). In general, class 1 leaf probability is:
    $$\text{leaf\_prob} = \frac{\text{value}[\text{node}, 0, 1]}{\sum_{c} \text{value}[\text{node}, 0, c]}$$

---

## 5. Prediction Path

### 5.1 Forest Evaluation ($p_{\text{raw}}$) **[VERIFIED IN CODE: `sklearn/ensemble/_forest.py`]**
1. **Float32 Cast:** Scikit-learn cast input features `X` to `np.float32` during tree traversal comparisons:
   $$\text{feat\_val} = \text{float32}(\text{feature}[f])$$
   The comparison with node threshold $T \in \text{float64}$ is:
   $$\text{if } \text{feat\_val} \le T \implies \text{go to left child}; \quad \text{else } \implies \text{go to right child}$$
   *Crucial for Kotlin equivalence:* The Kotlin evaluator must cast each double feature to Float (32-bit IEEE 754) before comparing with the tree threshold.
2. **Tree Traversal:** Start at node 0. Traverse until `children_left[node] == -1`.
3. **Forest Aggregation:** Average the class-1 leaf probabilities across all $N = 300$ trees:
   $$p_{\text{raw}} = \frac{1}{300} \sum_{t=1}^{300} \text{leaf\_prob}_t$$

### 5.2 Platt Calibration ($p_{\text{cal}}$) **[VERIFIED IN CODE: `ml/models/calibrator.json`, `ml/src/account_risk/calibrate.py`]**
Constants from `ml/models/calibrator.json`:
- `slope`: $0.7788447377548848$
- `intercept`: $-0.10476139794002066$
- `clip_low`: $0.001$
- `clip_high`: $0.999$

Evaluation formula:
1. **Clip:**
   $$p_{\text{clipped}} = \max(\text{clip\_low}, \min(\text{clip\_high}, p_{\text{raw}}))$$
2. **Logit:**
   $$x = \ln\left(\frac{p_{\text{clipped}}}{1.0 - p_{\text{clipped}}}\right)$$
3. **Linear transform:**
   $$z = \text{slope} \cdot x + \text{intercept}$$
4. **Sigmoid (expit):**
   $$p_{\text{cal}} = \frac{1.0}{1.0 + \exp(-z)}$$
5. In Python, `apply_platt` bounds the output strictly in $(0, 1)$ via `np.nextafter`.

### 5.3 Abstention Rule ($\tau = 0.78$) **[VERIFIED IN CODE: `ml/models/abstention.json`, `ml/src/account_risk/abstain.py`]**
- **Confidence Definition:**
  $$\text{confidence} = \max(p_{\text{cal}}, 1.0 - p_{\text{cal}})$$
- **Threshold $\tau$:** $0.78$.
- **Tie Rule:** Confidence equal to $\tau$ is answered ($\ge \tau$).
- **State Assignment:**
  - If input is unsupported $\rightarrow$ `"unsupported_input"`
  - If $\text{confidence} \ge \tau$ ($0.78$) $\rightarrow$ `"supported_prediction"`
  - If $\text{confidence} < \tau$ ($0.78$) $\rightarrow$ `"uncertain_prediction"`
- **Binary Label Rule:**
  - If $p_{\text{cal}} \ge 0.5 \implies \text{Label} = \text{"risk"}$ (RiskBand.HIGHER)
  - If $p_{\text{cal}} < 0.5 \implies \text{Label} = \text{"genuine"}$ (RiskBand.LOWER)

---

## 6. Wording Constraints (Rule G7) **[VERIFIED IN CODE: `ml/src/account_risk/predict.py`, `docs/wording_guide.md`]**
- The screen and all user-facing strings must use: **"resemblance to the research patterns"**.
- Forbidden terms in UI, logs, and user-facing assets:
  - `fake`
  - `bot` (except when naming external historical datasets like Cresci-2017)
  - `malicious`
  - `verified`
  - `authentic`
  - `secure`
  - `protected`
  - `tampered`
  - `forged`
- Never show raw probability, percentage, or score to the user. Only the band is presented.

---

## 7. Model Limits for User Presentation **[VERIFIED IN CODE: `ml/docs/limitations.md`, `ml/docs/model_card.md`]**
1. **Platform Shift:** Trained only on Instagram (March 2019 scrape, 574 accounts). AUROC drops from $0.9658$ on Instagram test to $0.8227$ on Twitter (Cresci-2017). The app must enforce **Instagram only**; other platforms must show "Not validated for this platform".
2. **Selective Coverage:** Recall across bot architectures in external benchmarks ranged from $99.57\%$ down to $0.00\%$ (`traditional_spambots_4`).
3. **Abstention Limitation:** Abstention measures internal model confidence, not distribution shift or adversarial tampering. A model can be wrong even when confident.
4. **No Identity Attribution:** Relies on 5 public profile fields; has no visibility into private metadata, messages, IP addresses, or post content.

---

## 8. Freeze Identity **[VERIFIED IN CODE: `ml/models/freeze_manifest.json`, `predict.py`]**
- **Freeze Commit in Manifest:** `1a83dc13c40f88228ae8f470d07e57ffc34206c1`
- **Freeze Commit in `predict.py` constant:** `c014a1264a5648a48ad59713910ff1c694606f87` (tagged `stage2-freeze`)
- **Freeze Manifest SHA-256 (canonical recorded):** `002b72b1593e3485f0f6bcdf0a3e450e77658e7d5766924224e8a0b4c232c514`
- **Current `freeze_manifest.json` file hash:** `e6683dcaf1136e5e13e8509209520d676b102e1f90f4b29073e4ff41e6ea8ba7` (passes `04_freeze.py verify` with `OK`)
- **Library Versions:**
  - `python`: `3.11.9`
  - `scikit-learn`: `1.9.1`
  - `numpy`: `2.4.6`
  - `pandas`: `3.0.6`
  - `scipy`: `1.17.1`
  - `joblib`: `1.6.0`
  - `shap`: `0.51.0`

---

## 9. Questions for Abdul **[INFERENCE]**
1. **Scaler:** Confirmed via inspection that `rf_pipeline.joblib` contains only `('model', RandomForestClassifier)`. There is no scaler (`hasScaler = 0`).
2. **Platt Constants:** Confirmed slope $= 0.7788447377548848$ and intercept $= -0.10476139794002066$. Note: Model card text in `ml/docs/model_card.md` line 16 has an old text typo citing slope 1.2505 and intercept -0.1518, but `calibrator.json` and `predict.py` use the verified values ($0.77884$ and $-0.10476$). The code and `calibrator.json` are authoritative.
3. **Manifest SHA-256:** `predict.py` defines `MANIFEST_SHA256 = "002b72b1593e3485f0f6bcdf0a3e450e77658e7d5766924224e8a0b4c232c514"`. Both this hash and the file hash will be recorded in the export manifest.
