# Ledger

A minimalist budget planner and expense tracker for the Light Phone III, built with the Light SDK.

## Features

**Budget Planning**
- Set monthly budget targets per category group (Housing, Food, Transportation, etc.)
- Budget amounts carry over month-to-month automatically
- Progress bars on the home screen show spent vs. budgeted

**Expense Tracking**
- Log income, fixed expenses, variable expenses, and debts
- Frequency tags: One-time, Weekly, Biweekly, Bimonthly, Monthly, Quarterly, Annually
- Add optional notes to any entry
- Navigate to any month to backtrack entries

**Categories**
- 12 category groups with 50+ default subcategories based on standard household budgeting
- Savings & Investments group (Savings Account, 401k/403B, IRA, Stocks)
- Add, rename, or delete custom categories
- Hide/show default categories to match your needs

**Currency**
- 60+ currencies across 7 regions (Americas, Europe, Asia-Pacific, Africa, Middle East)
- Collapsible region picker

**Security**
- Optional 4-digit PIN lock

## Screenshots

<p align="center">
  <img src="screenshots/homescreen.png" width="200" />
  <img src="screenshots/categories.png" width="200" />
  <img src="screenshots/entry.png" width="200" />
</p>
<p align="center">
  <img src="screenshots/history.png" width="200" />
  <img src="screenshots/settings.png" width="200" />
</p>

## Installation

### From Releases (Obtanium / sideload)
1. Download the latest `.apk` from [Releases](../../releases)
2. Install via ADB:
   ```
   adb install ledger-v1.0.0-release.apk
   ```

### Build from source
1. Clone this repository
2. Add your GitHub token for Light SDK packages:
   ```
   echo "gpr.user=YOUR_GITHUB_USERNAME" >> local.properties
   echo "gpr.key=YOUR_GITHUB_TOKEN" >> local.properties
   ```
   The token needs the `read:packages` scope. Generate one at [GitHub Settings → Tokens](https://github.com/settings/tokens/new).
3. Build:
   ```
   ./gradlew :tool:assembleDebug
   ```
4. Install:
   ```
   adb install -r tool/build/outputs/apk/debug/tool-debug.apk
   ```

## Known Issues

- **Sleep prevention:** The tool may prevent the phone from entering sleep mode while active. Under investigation — the SDK's `LightActivity` does not set `FLAG_KEEP_SCREEN_ON`, so the cause may be in the embedded keyboard or continuous Compose recomposition.
- **PIN on sub-screens:** If the app is backgrounded while on a sub-screen (Settings, History), the PIN re-locks when you return to the home screen, not immediately on the sub-screen. This is an SDK limitation.

## License

MIT

## Credits

Built for the Light Phone III community. Icon design included.
Uses the [Light SDK](https://github.com/lightphone/light-sdk) by The Light Phone.
