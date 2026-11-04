# Tasks

## 1. System date in the controller

- [x] 1.1 Add the `LocalDate currentDate` field, the `CommandProcessor(UserManager, LocalDate)` constructor (the existing one delegates with `LocalDate.now()`) and `getCurrentDate()`; verify with a `CommandProcessorTest` case that a new processor reports the injected startup date and the default constructor reports today's date
- [x] 1.2 Implement `SYSTEM DATE;yyyy-MM-dd` with strict `uuuu-MM-dd` parsing, parameter-count check and no change on failure, and add it to `HELP_LINES`; verify with `CommandProcessorTest` cases for a valid date, backward/forward moves, the 2028-02-29 / 2026-02-29 boundary, every invalid format from the spec, wrong parameter counts and HELP content

## 2. Integration checks

- [x] 2.1 Run `mvn clean test` (all previous tests pass) and run the packaged JAR with a file containing `SYSTEM DATE;2026-09-15`, `SYSTEM DATE;2026-02-30` and `HELP`; confirm the OK/ERROR lines and that `LocalDate.now()` appears only in the controller's default constructor
