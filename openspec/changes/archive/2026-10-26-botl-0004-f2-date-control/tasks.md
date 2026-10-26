# Tasks

## 1. System date in the controller

- [x] 1.1 Add `currentDate` to `CommandProcessor` (initialised from the OS date, test constructor with explicit date, `getCurrentDate()`); verify with a test that a new processor starts on `LocalDate.now()`
- [x] 1.2 Implement `SYSTEM DATE;yyyy-MM-dd` with strict parsing and unchanged date on error; verify with `CommandProcessorTest` cases (valid, backward, 15/09/2026, 2026-9-15, 2026-02-30, missing parameter)
- [x] 1.3 Add `SYSTEM DATE;yyyy-MM-dd` to HELP; verify the HELP test checks it and `mvn test` passes
