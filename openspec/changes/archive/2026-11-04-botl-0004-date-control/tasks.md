# Tasks

## 1. System date

- [x] 1.1 Add the `currentDate` field initialised from `LocalDate.now()` (plus an injectable-date constructor) and `getCurrentDate()` to `CommandProcessor`; verify with `CommandProcessorTest` that a new processor reports today's date
- [x] 1.2 Implement `SYSTEM DATE;yyyy-MM-dd` with strict parsing and unchanged date on error; verify with `CommandProcessorTest` (valid date, backward/forward, malformed, impossible, wrong parameter count)
- [x] 1.3 Add `SYSTEM DATE;yyyy-MM-dd` to the HELP `USAGE` list; verify with the HELP content test

## 2. Regression

- [x] 2.1 Run `mvn clean package` with all tests passing, check that no production code other than the controller initialisation calls `LocalDate.now()`, and run `SYSTEM DATE` through the packaged JAR
