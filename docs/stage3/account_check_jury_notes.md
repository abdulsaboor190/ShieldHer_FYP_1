# ShieldHer Account Check — Jury Defense Notes

**Date:** 25 November 2026  
**Audience:** FYP-I Evaluation Jury / Defense Panel  
**Presenters:** Abdul (Model & ML Lead), Gotam (UI/UX & Front-End Lead), Bhoomika (Vault & Storage Lead)

---

## 1. Executive Summary (Two Sentences)
Account Check allows someone concerned about an online profile to enter high-level profile metadata and immediately evaluate its statistical resemblance to known research patterns. The check runs 100% on-device in real time without network connectivity, storing nothing typed and presenting honest qualitative bands rather than misleading certainty scores.

---

## 2. Architecture & Design Rationale

- **Why On-Device?**
  Privacy and trust are paramount for victims of harassment or impersonation. Running on-device means no typed profile data, usernames, or metadata leave the phone. There is zero network footprint, no server backend dependency, and no `android.permission.INTERNET` in the application manifest.
- **Why Qualitative Bands Instead of Percentages?**
  Presenting a number like "83.7%" gives a false sense of mathematical certainty. Machine learning models evaluate statistical resemblance to historical training distributions, not truth or guilt. Bands ("Higher resemblance", "Lower resemblance", "Not enough confidence to say") accurately communicate the model's output without misleading users into false confidence or panic.
- **Why Instagram Only?**
  The model was trained and rigorously validated on Instagram profile data. While synthetic bot topologies exist on other platforms, cross-platform transfer cannot be scientifically guaranteed without empirical validation. Selecting X (Twitter) or Other transparently informs the user that the model is "Not validated for this platform".
- **Why Amber and Grey Only (No Green or Red)?**
  In ShieldHer's design system, **green** is strictly reserved for cryptographic certainty ("Unchanged since saved" verified by SHA-256), and **red** is reserved for evidence alteration or file loss. Using green for an account would falsely suggest an account is "safe" or "verified", while red would make an unsubstantiated accusation of malicious intent.

---

## 3. Quantitative Evidence (Stage 2 Validation Baseline)

The on-device model (`account_risk_model_v1.bin`) is an exact serialization of the frozen Stage 2 model:
- **Architecture:** 300 decision trees, 5 features (`followers`, `following`, `description_length`, `external_url`, `follower_following_ratio`), Platt scaling calibrated.
- **Instagram Test Set:**
  - **AUROC:** 0.9658 (evaluated on 118 independent accounts).
- **Cross-Domain Generalization (Cresci-2017 Benchmark):**
  - **AUROC:** 0.8227.
  - **Group Recall Range:** From 0.9957 down to 0.0000 across heterogeneous social spam groups.
  - **Abstention Mechanism:** Tau threshold ($\tau = 0.78$) enables abstention ("Not enough confidence to say") instead of issuing incorrect high-risk warnings when encountering unfamiliar patterns.

---

## 4. Likely Jury Questions & Defensible Answers

### Q1: Does Account Check work on other platforms like Twitter/X or TikTok?
> **Answer:** No, it is intentionally restricted to Instagram. The underlying model was trained and validated specifically on Instagram profile feature distributions. Applying it to Twitter/X or TikTok would represent an unvalidated domain transfer. The UI transparently indicates "Not validated for this platform" rather than producing an untrustworthy estimate.

### Q2: Why don't you display the exact risk percentage or probability score?
> **Answer:** Displaying a probability like 87% creates false precision. A calibrated model score represents resemblance to a specific historical training distribution, not the objective probability that a live account is authentic or illegitimate. Categorical bands provide clear guidance while adhering to ethical AI principles and avoiding algorithmic over-reliance.

### Q3: Does ShieldHer send the typed account information to a server or external API?
> **Answer:** No. ShieldHer contains zero networking code, does not request the `INTERNET` permission, and runs the entire 300-tree evaluation locally on the device CPU in under 15 milliseconds. Furthermore, inputs exist in transient memory only; bio text is converted to a code point count and immediately discarded, and all fields are purged when the screen is navigated away from or destroyed.

### Q4: How do you know the on-device Kotlin evaluator gives the exact same result as your Python Stage 2 model?
> **Answer:** We generated a 1,090-vector golden test set from the frozen Stage 2 environment covering boundary cases, random distributions, edge inputs, and unsupported inputs. Parity testing between Python scikit-learn and our pure Kotlin tree evaluator confirmed an exact 1,090 / 1,090 match, with a maximum floating-point deviation of less than $1 \times 10^{-15}$, well within IEEE 754 precision limits.

### Q5: What happens when the model is unsure about an account?
> **Answer:** The model incorporates confidence-based abstention with an uncertainty threshold ($\tau = 0.78$). When the calibrated probability falls in the ambiguous middle range, the app abstains and displays "Not enough confidence to say". This ensures users are not alerted with false alarms when the model cannot make a reliable determination.
