# Tasks

## 1. Main controller and date

- [x] 1.1 Rename `UserException` to `ParkException` across main and test code; verify `mvn test` still passes unchanged otherwise
- [x] 1.2 Create `MainController` with `Clock`-based initial date, `getCurrentDate`/`setCurrentDate`, and strict `parseDate` (`uuuu-MM-dd`); verify `MainControllerTest` (initial date from a fixed clock, valid date, `2026-9-15`, `15/09/2026`, `2026-02-30` rejected)

## 2. SYSTEM DATE command

- [x] 2.1 Wire `MainController` into `CommandProcessor` and `App.main`, add `SYSTEM DATE;yyyy-MM-dd` to `COMMAND_SYNTAX` and implement it (`OK SYSTEM DATE SET -> <date>`, date unchanged on error); verify `CommandProcessorTest` covers set forward/backward, invalid format, missing parameter, and HELP listing the command
- [x] 2.2 Verify no production code other than `MainController` calls `LocalDate.now`/`Clock` (grep), then run `mvn package` and an end-to-end JAR run with `SYSTEM DATE` valid/invalid commands
