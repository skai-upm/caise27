# Tasks

## 1. Model

- [x] 1.1 Add `PassLookup`, `EntryResult`, `individualTickets()`, the pass-aware atomic `admit` and the `passesToConsume` hook with the `SpecialAttraction` override; verify with `PassConsumptionTest` at model level (atomicity, one pass per component, special rules)

## 2. Commands

- [x] 2.1 Inject `PassRepository` into `AttractionController`, support `ATTRACTION ENTER;…;SPEED`, report consumed passes and list the variant in HELP; verify every attraction-access scenario of this change in `PassEntryCommandsTest` and keep existing entry tests passing

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C08] Extend attraction access and pass consumption`; verify with `git log -1 --format=%s`
