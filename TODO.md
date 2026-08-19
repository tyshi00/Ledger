# Ledger - Outstanding Items

## Backup & Restore
- Removed from the tool temporarily. LightFileShare writes to a shared directory
  but the file wasn't accessible to the user. Needs investigation into how LightOS
  exposes shared files (possibly via a file manager tool or adb pull).
- The backup format is a pipe-delimited text file with sections for entries, budgets,
  custom categories, and preferences.
- Import logic was written and tested. Parses the backup file back into Room.
- Consider: JSON export via kotlinx.serialization for a cleaner format.

## PIN Screen Flash
- When returning to the app after backgrounding, the content briefly flashes for one
  frame before the PIN pad renders.
- Root cause: Android restores the saved visual state (captured during
  onSaveInstanceState) before any app code executes. The system task snapshot
  is captured at a point we cannot intercept from tool code.
- Attempted fixes:
  - StateFlow with collectAsState (Compose caches old state on resume)
  - Direct StateFlow.value read (composable body not re-executed on first resume frame)
  - Native View overlay via AndroidView (ProcessLifecycleOwner onPause + direct
    View.visibility, no post delay)
- The proper fix requires FLAG_SECURE on the Activity window or overriding
  onSaveInstanceState to hide the content view. Both require access to the Activity
  or Window object, which the Light SDK restricts (android.app.* is blocked).
- This is the same limitation that led Molly to use setAllViewsWithContentHidden()
  via direct Activity access, which is unavailable in the Light SDK environment.
