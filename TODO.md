# Ledger — Outstanding Items

## Backup & Restore
- Removed from the tool temporarily. `LightFileShare` writes to a shared directory
  but the file wasn't accessible to the user. Needs investigation into how LightOS
  exposes shared files (possibly via a file manager tool or adb pull).
- The backup format is a pipe-delimited text file with sections for entries, budgets,
  custom categories, and preferences.
- Import logic was written and tested — parses the backup file back into Room.
- Consider: JSON export via kotlinx.serialization for a cleaner format.

## PIN Lock on App Background
- Currently uses a session flag + internal navigation flag.
- Works for HomeScreen backgrounding but not for sub-screen backgrounding.
- Possible improvement: `ProcessLifecycleOwner` from `androidx.lifecycle:lifecycle-process`
  to detect app foreground/background globally. Needs dependency check against SDK allowlist.

## Sleep Prevention
- The tool may prevent the phone from entering sleep mode while active.
- SDK's `LightActivity` does NOT set `FLAG_KEEP_SCREEN_ON`.
- Possible causes: embedded keyboard holding focus, continuous Compose recomposition
  from StateFlow collection, or the LP3 keyboard library's animation loop.
- Compare behavior with other tools (Trinkets, Aventura) to isolate whether this is
  SDK-level or tool-specific.
- Potential fix: add `android:keepScreenOn="false"` to a root view, or ensure all
  StateFlows use `distinctUntilChanged()`.
