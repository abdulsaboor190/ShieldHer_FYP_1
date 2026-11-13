# Machine Note: Abdul ML (Stage 2 Verification)

Date: 2026-11-13

## Environment
- **Python Version:** 3.11.9 (CPython, 64-bit Windows)
- **Virtual Environment:** `D:\23i-3039\P2\ShieldHer1\ml\.venv`

## Package Versions (`pip list`)
| Package | Version |
| :--- | :--- |
| python | 3.11.9 |
| scikit-learn | 1.9.1 |
| pandas | 3.0.6 |
| numpy | 2.4.6 |
| shap | 0.51.0 |
| joblib | 1.6.0 |
| scipy | 1.17.1 |
| pytest | 9.1.1 |
| matplotlib | 3.11.2 |

## Freeze Verification (`python scripts/04_freeze.py verify`)
```text
[verify] Loaded freeze_manifest.json. Running integrity checks...
[verify] OK — all frozen files match their manifest hashes.
```

## Test Suite Results (`pytest`)
- **Status:** 461 passed, 3 warnings in 27.21s
- **Warnings:** 3 benign PendingDeprecationWarning notices from `shap` color map utilities (`set_bad`, `set_over`, `set_under`).

## Inference Interface: `predict_one`
- **File:** `src/account_risk/predict.py`
- **Function Signature:**
  ```python
  def predict_one(
      features: dict[str, Any],
      bundle: dict[str, Any] | None = None,
  ) -> dict[str, Any]:
  ```
- **Expected Inputs:**
  Dictionary of account features. Expected keys: `followers`, `following`, `description_length`, `external_url`. (Missing or None values are handled per D-004 rules; extra keys are ignored).

## Synthetic Account Sample Prediction
- **Input:**
  ```python
  {
      'followers': 350,
      'following': 420,
      'description_length': 65,
      'external_url': 0
  }
  ```
- **Output:**
  ```python
  {
      'confidence': 0.9754849211902483,
      'invalid_features': [],
      'metadata': {
          'calibration_status': 'Platt scaling fit on out-of-fold Instagram predictions, valid for the Instagram distribution only, assumes about a 50% base rate',
          'confidence_definition': 'max(p, 1-p) on the calibrated probability',
          'disclaimer': 'risk_probability is the probability that an account resembles benchmark-defined risk patterns, on the Instagram distribution at a 50% base rate. It does not determine whether an account is genuine or responsible for harassment. Abstention measures the model\'s confidence and is not a check that an account looks like the training data.',
          'freeze_commit': 'c014a1264a5648a48ad59713910ff1c694606f87',
          'library_versions': {
              'joblib': '1.6.0',
              'numpy': '2.4.6',
              'pandas': '3.0.6',
              'python': '3.11.9',
              'scikit-learn': '1.9.1',
              'scipy': '1.17.1',
              'shap': '0.51.0'
          },
          'manifest_sha256': '002b72b1593e3485f0f6bcdf0a3e450e77658e7d5766924224e8a0b4c232c514',
          'mask_name': 'A',
          'model_features': [
              'followers',
              'following',
              'description_length',
              'external_url',
              'follower_following_ratio'
          ],
          'model_version': 'v1',
          'tested_conditions': 'development estimates by cross-validation on Instagram; held-out results on the Instagram test set and on Cresci-2017 are in ml/docs/stage2_results.md; the model was not tested on any other platform and its performance varied strongly by account type on Cresci-2017',
          'threshold': 0.78,
          'tie_rule': 'confidence equal to tau is answered',
          'training_domain': 'Instagram training dataset (train.csv): 574 balanced accounts (287 genuine, 287 risk)'
      },
      'missing_features': [],
      'reason': '',
      'risk_probability': 0.02451507880975169,
      'state': 'supported_prediction'
  }
  ```

