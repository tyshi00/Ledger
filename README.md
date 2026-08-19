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
- Savings & Investments group (Savings for a Goal, Emergency Savings, 401k/403B, IRA, Stocks)
- Add, rename, or delete custom categories
- Hide/show default categories to match your needs

**Currency**
- 60+ currencies across 7 regions (Americas, Europe, Asia-Pacific, Africa, Middle East)
- Collapsible region picker

**Security**
- Optional 4-digit PIN lock
- Locks automatically when the app is backgrounded from any screen

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
   adb install ledger-v1.1.0-release.apk
   ```

### Build from source
1. Clone this repository
2. Add your GitHub token for Light SDK packages:
   ```
   echo "gpr.user=YOUR_GITHUB_USERNAME" >> local.properties
   echo "gpr.key=YOUR_GITHUB_TOKEN" >> local.properties
   ```
   The token needs the `read:packages` scope. Generate one at [GitHub Settings > Tokens](https://github.com/settings/tokens/new).
3. Build:
   ```
   ./gradlew :tool:assembleDebug
   ```
4. Install:
   ```
   adb install -r tool/build/outputs/apk/debug/tool-debug.apk
   ```

## Known Issues

- **PIN screen flash:** When returning to the app after backgrounding, the underlying screen content may briefly flash for one frame before the PIN pad appears. This is an Android framework limitation where the system restores the saved visual state before any app code executes. The proper fix (FLAG_SECURE on the Activity window) requires access to APIs that the Light SDK restricts.

## License

MIT

## Credits

Built for the Light Phone III community. Icon design included.
Uses the [Light SDK](https://github.com/lightphone/light-sdk) by The Light Phone.
PIN lock pattern inspired by [Molly](https://github.com/nickmartens/molly-light) (Signal fork for Light Phone).
