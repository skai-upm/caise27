# Design

## Context

All Delivery 2 commands exist. `App.runWithStorage` registers four command groups inline. Users exist only as e-mails behind `UserDirectory` (empty at runtime). See proposal.md - Why and Known gap.

## Decisions

- **`App.registerCommands(CommandProcessor, ParkController, PrintStream)`** is the single public wiring method used by `App` and by the integration test. A test that hand-wires its own processor could pass while the real application is broken.
- **Integration test** drives the full flow through the real wiring and a `ParkStorage` in a temp directory, with a test `UserDirectory` injected after wiring (the only stub, required by the known gap). It then restarts by saving, resetting and loading, and checks the rebuilt state. A separate `App.run` test covers the phase-2 file in `--fly` mode, where all user-independent commands succeed and no data remains.
- **Restriction tests** use reflection over declared field types (no external libraries): `Group`/`Attraction` have no `Ticket` field types (including generic collections, checked via `getGenericType().getTypeName()`), and `GroupTicket` has `List<IndividualTicket>` and `Group` fields.
- **HELP text** is replaced with the Delivery 2 list in the source's order. Existing tests asserting Delivery 1 placeholders are updated to the new text.
