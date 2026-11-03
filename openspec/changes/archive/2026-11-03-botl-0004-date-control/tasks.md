# Tasks

## 1. Current date in the controller

- [x] 1.1 Add the `currentDate` field initialised from `LocalDate.now()` (plus a constructor taking an initial date) and `getCurrentDate()` to `CommandProcessor`; verify a test that a new processor reports today's date and that a processor built with a fixed date reports it

## 2. SYSTEM DATE command

- [x] 2.1 Implement `SYSTEM DATE;yyyy-MM-dd` with strict `uuuu-MM-dd` parsing, rejecting wrong formats, impossible dates and missing/extra parameters without changing the date; verify `CommandProcessorTest` cases for 2026-09-15, backward/forward changes, `15/09/2026`, `2026-9-15`, `hoy`, `2026-02-30` and `SYSTEM DATE`
- [x] 2.2 Add `SYSTEM DATE;yyyy-MM-dd` to the HELP list; verify the help test checks all six commands

## 3. Integration verification

- [x] 3.1 Verify `AppTest` runs `SYSTEM DATE` from a file and from standard input with identical output, confirm with `grep` that `LocalDate.now()` is used only in the controller's default constructor, and `mvn clean test` passes
