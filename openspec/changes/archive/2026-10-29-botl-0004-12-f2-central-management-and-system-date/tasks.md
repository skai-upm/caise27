# Tasks

## 1. Central park management

- [x] 1.1 Create `ParkController` singleton (private constructor, holder idiom, `getInstance()`) holding current date (`LocalDate.now()` at start) and base price `40.00`, with package-private `reset()`; verify with unit tests that two `getInstance()` calls return the same object, no public constructor exists, and defaults are today's date and 40.00
- [x] 1.2 Add date setting with strict `uuuu-MM-dd` parsing and price setting with positive, max-two-decimal validation (no regular expressions); verify with unit tests for valid values and for `2027-02-30`, `15-04-2027`, `0`, `-3`, `abc`, `45,50`, `1.234` leaving state unchanged

## 2. SYSTEM commands

- [x] 2.1 Dispatch `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` in `CommandProcessor` (exactly two fields, confirmation or error message); verify with processor tests for `SYSTEM DATE;2027-04-15`, repeated date changes, `SYSTEM PRICE;45.50`, invalid values and missing arguments
- [x] 2.2 Add both syntaxes to HELP; verify with a test asserting `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` appear in the help output
- [x] 2.3 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR twice (`SYSTEM PRICE;45.50` then a fresh run) and verify the second execution starts from 40.00 via a fresh-instance test of defaults
