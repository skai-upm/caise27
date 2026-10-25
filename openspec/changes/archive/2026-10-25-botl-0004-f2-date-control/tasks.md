# Tasks

## 1. Current date

- [x] 1.1 Create `SystemDate` (get/set, strict `parse` returning null on invalid input); verify with `SystemDateTest` (valid date, wrong formats, impossible date, set backwards/forwards)
- [x] 1.2 Add `SYSTEM DATE;yyyy-MM-dd` to `CommandProcessor` (owns the `SystemDate`, `getSystemDate()`, `OK SYSTEM DATE -> <date>`, `ERROR INVALID DATE`, parameter count) and to the `HELP` list; create the date with `LocalDate.now()` in `App.start`; verify with `CommandProcessorTest` and `AppTest`, and that `mvn package` passes
