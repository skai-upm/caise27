# Design

## Decisions

- **Same wiring as the application**: the integration test uses `App.registerCommands` plus `ParkStorage.inDirectory` injected into `ParkController`, exactly as `App` does. The only stub is the `UserDirectory`, which is required by the known gap.
- **Restart check**: save, `reset()`, re-inject and load. It asserts attraction types, pass prices and consumed states, and group-ticket compositions.
- **`--fly` check** through `App.run` with `input_fase3.txt` in a temp base directory. `tmp/` must be empty afterwards and `data/` must not exist.
- **Restriction checks** by reflection: `UserInvoiceBuilder`/`GroupInvoiceBuilder` extend `InvoiceBuilder`; `ParkController` has no public constructor while `ParkStorage` has one; the generic superclass type arguments of the attraction subclasses are the access interfaces; a consumed pass is still found after use.
