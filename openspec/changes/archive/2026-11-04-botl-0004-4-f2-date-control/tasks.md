# Tasks

## 1. Current date in the controller

- [x] 1.1 Add `currentDate` with `getCurrentDate()` to `CommandProcessor`, the `(UserManager, LocalDate)` constructor and the delegating one-argument constructor using `LocalDate.now()`; verify with `CommandProcessorTest` that a new processor reports the injected date and the default constructor reports today's local date

## 2. SYSTEM DATE command

- [x] 2.1 Implement `SYSTEM DATE;yyyy-MM-dd` with strict parsing, usage errors for missing/extra parameters, and add its syntax to HELP; verify with `CommandProcessorTest` for the valid example, backward/forward moves, leap day, every invalid example leaving the date unchanged, and HELP listing `SYSTEM DATE`

## 3. Regression

- [x] 3.1 Verify `mvn test` passes, no production class other than the controller's default constructor calls `LocalDate.now()` (grep), and a packaged-JAR run of `SYSTEM DATE;2026-09-15` then `HELP` then `EXIT` succeeds
