# Proposal Deviations
FE-6.2

## Stage 3 changes to Stage 1 files
- **Deleted template MainActivity.kt**: Deleted default Android Studio starter template `app/src/main/java/com/shieldher/MainActivity.kt` and removed its stub `<activity android:name=".MainActivity" />` entry from `AndroidManifest.xml`. Replaced by `com.shieldher.dashboard.MainActivity`.
- **Launcher intent filter move**: Moved `android.intent.action.MAIN` and `android.intent.category.LAUNCHER` from `com.shieldher.vault.ui.EvidenceListActivity` to `com.shieldher.dashboard.MainActivity`. `EvidenceListActivity` remains registered with `android:exported="true"`.
- **Shell layout replacement**: Replaced template `app/src/main/res/layout/activity_main.xml` (empty ConstraintLayout with "Hello World!") with the dashboard shell layout (LinearLayout with `status_bar_spacer`, `nav_host` FragmentContainerView, and `bottom_nav` BottomNavigationView). Removed from token check exclusions (`scripts/token_check_stage1_layouts.txt`) in FE-3.1d fix.
- **Theme, colour tokens, and strings**: Appended Material 3 Light theme (`Theme.ShieldHer`) in `themes.xml` and `values-night/themes.xml`, added design tokens in `colors.xml`, added typography/spacing tokens in `dimens.xml` and `styles.xml`, and added dashboard string keys in `strings.xml`.
- **Vault files untouched**: No file under `app/src/main/java/com/shieldher/vault/` was changed or deleted.

## Account Check integration in FYP-I
- **Change from Original Plan (D3 Reversal):** Decision D3 initially planned Account Check as a static placeholder layout for FYP-I with real model outputs shown only in jury presentation slides. In Stage 3 (Decision D18), the frozen 300-tree Random Forest model from Stage 2 was ported to an on-device Kotlin evaluator and fully integrated into the Android app.
- **Why Enabled in FYP-I:** Full parity testing on 1,090 golden vectors demonstrated that the pure Kotlin evaluator achieves exact numerical equivalence ($\le 1.0 \times 10^{-12}$ difference) to Python scikit-learn without requiring any backend server or network connectivity.
- **Zero Network Overhead:** The integration runs entirely locally using `app/src/main/assets/account_risk_model_v1.bin`. No network libraries or internet permissions (`android.permission.INTERNET`) were introduced.
- **Remaining FYP-II Scope:** Feature importance explanations (TreeSHAP) and saving account evaluations to vault cases remain deferred to FYP-II, as feature stability under distribution shifts and evidence schema expansions were not part of Stage 2 freeze.

