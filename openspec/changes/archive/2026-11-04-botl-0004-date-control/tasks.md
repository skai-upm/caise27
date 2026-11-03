# Tasks

## 1. System date in the controller

- [x] 1.1 Add `currentDate` (initialised with `LocalDate.now()` via a constructor taking the initial date) and `getCurrentDate()` to `CommandProcessor`; verify a test that a new processor reports today's date and the explicit constructor reports the given date
- [x] 1.2 Implement `SYSTEM DATE;yyyy-MM-dd` with strict parsing and unchanged date on error, dispatch by exact operation name, and add it to `COMMAND_SYNTAX`; verify `CommandProcessorTest` for valid set, backward/forward moves, wrong formats, impossible dates, missing/extra parameter, and HELP content

## 2. Regression and integration

- [x] 2.1 Verify through `AppTest` that a file run with `SYSTEM DATE` gives `OK:`/`ERROR:` responses and a fresh run starts again at the OS date, and that `mvn test` and `mvn package` succeed with all previous tests passing
