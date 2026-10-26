# Tasks

## 1. System date

- [x] 1.1 Create `SystemDate` (shared mutable current date, `today()` factory, strict `yyyy-MM-dd` parsing); verify with unit tests for valid, wrong-format and impossible dates
- [x] 1.2 Add `SYSTEM DATE;yyyy-MM-dd` to `CommandProcessor` (and HELP), wire one `SystemDate` from `App`; verify with unit tests that valid dates are applied (forward and backward) and invalid ones leave the date unchanged
- [x] 1.3 Run `mvn test` and a jar session with `SYSTEM DATE` commands; verify all tests pass and responses are as specified
