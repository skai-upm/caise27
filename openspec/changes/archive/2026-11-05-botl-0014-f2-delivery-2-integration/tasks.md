# Tasks

## 1. HELP and wiring

- [x] 1.1 Replace `HELP_TEXT` with the Delivery 2 syntax list and update existing help assertions; verify a test asserts every listed line verbatim
- [x] 1.2 Extract `App.registerCommands` and use it in `App`; verify existing `App` tests still pass

## 2. Integration and restrictions

- [x] 2.1 Add `Delivery2IntegrationTest` (full flow through the real wiring, persistence restart, `--fly` phase-2 file with cleanup); verify it passes
- [x] 2.2 Add `Delivery2RestrictionsTest` (relationship directions via reflection, ticket abstraction, singleton); verify it passes

## 3. Verification

- [x] 3.1 Run `mvn -q package`, then run the JAR with a phase-2 command file in normal and `--fly` modes; verify all tests pass and no `tmp/` data remains
