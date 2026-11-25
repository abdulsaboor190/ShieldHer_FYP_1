# Wording Guide
FE-1.4

## Tone

Calm, plain, supportive, non-judgemental; short sentences; users may be in distress.

## Vocabulary Guidelines

| Use | Avoid |
|---|---|
| "Saved to this phone" | "Secured", "Protected" (unless exactly true) |
| "Unchanged since saved" | "Verified", "Authentic", "Proven" |
| "Changed or unreadable since saved" | "Tampered", "Forged" |
| "File missing" | "Deleted by user", "Corrupted" |
| "Not checked yet" | "Pending verification", "Unverified" |
| "Received by ShieldHer" / "You entered" | "Created on", "Sent on" |
| "Evidence deleted" | "Removed from vault", "Destroyed" |
| "Higher resemblance", "Lower resemblance", "Not enough confidence to say" | Any percentage, probability, or accusatory words: "fake", "bot", "malicious", "verified", "authentic", "secure", "protected", "tampered", "forged" |
| "Preview: not working yet" and "Coming in the next version" (Account Check) | *Superseded by D18 (on-device evaluation enabled for Instagram)* |

## Account Check Wording

### Allowed Band Titles (exact match to strings_risk.xml)
- **Higher resemblance:** "Higher resemblance to the research patterns"
- **Lower resemblance:** "Lower resemblance to the research patterns"
- **Uncertain / Not confident:** "Not enough confidence to say"
- **Unsupported input:** "Insufficient context or unsupported input"
- **Platform not validated:** "Not validated for this platform"

### Core Framing Rule (Stage 2 rule G7)
Always describe output as "resemblance to the research patterns". Never make definitive claims about account authenticity or identity.

### Words Never Used
Never use in copy or UI: `fake`, `bot` (as a whole word), `malicious`, `verified`, `authentic`, `secure`, `protected`, `tamper`, `forged`, or numeric probabilities/percentages (`%`).

### Fixed Text Blocks

#### Block 5: Limits and Context (risk_limits_body)
"Research patterns come from accounts studied in 2017 to 2020. This check looks only at the numbers and length you typed, not the profile picture, posts or messages. It cannot tell whether an account is real, impersonated or operated by a human."

#### Block 6: Model Line (risk_model_line)
"Model: 300-tree random forest (v1, frozen Nov 2026). Evaluated on this phone without network access."

## Four Fixed Text Blocks

### Block 1: What "Unchanged since saved" means
"The saved copy is identical to what ShieldHer received. It does not show who made it, when it was originally made, or whether it was edited before you shared it. It does not decide whether it can be used in a legal process."

### Block 2: Time
"'Received by ShieldHer' comes from this phone's clock. 'You entered' is a date you typed yourself."

### Block 3: Delete
"This removes the file and its record from this phone. It cannot be undone. Case notes that mention it stay, marked 'evidence deleted'."

### Block 4: Storage (Settings)
"Evidence files are stored encrypted, with keys held in the Android Keystore. Case notes are stored encrypted in a separate file. The vault's technical record of each item (receipt hash, receipt time, time zone, source label, file size and file path) is kept in the app's private storage in a database that is not encrypted."

Note: Do not add any sentence about Android backup until FE-0.3 confirms allowBackup=false.

## Review Rule

Abdul reviews every new string against this guide before a PR merges.
