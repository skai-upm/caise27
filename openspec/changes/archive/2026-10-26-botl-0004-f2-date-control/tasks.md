# Tasks

## 1. System date

- [x] 1.1 Create `SystemDate` (initialised to the OS date, `get`/`set`) and wire one instance from `App` into `CommandProcessor`; verify a unit test that a new instance equals `LocalDate.now()`
- [x] 1.2 Implement `SYSTEM DATE;yyyy-MM-dd` with strict parsing in `CommandProcessor`; verify tests for valid dates, moving backward/forward, wrong format, impossible date and missing parameter (date unchanged on error)
- [x] 1.3 Add `SYSTEM DATE;yyyy-MM-dd` to HELP; verify the HELP test checks it, and run `mvn test` to verify the full suite passes
