# Tasks

## 1. Central controller

- [x] 1.1 Create the `ParkController` singleton (private constructor, `getInstance`, `initialize(users, today)`, date and price accessors with validation) and verify with `ParkControllerTest`: same instance, no public constructor, initial date/price, price/date replacement and rejection of invalid prices

## 2. SYSTEM commands

- [x] 2.1 Initialise the controller in `App.run` and add `SYSTEM DATE` / `SYSTEM PRICE` (and their HELP lines) to `CommandProcessor`; verify with `CommandProcessorTest` valid and invalid dates (`2027-02-30`, `15-04-2027`, missing value), valid and invalid prices (`0`, `-5`, `abc`, `10.123`) and HELP content
- [x] 2.2 Verify with `AppTest` that a price set in one run is back to 40.00 in the next run and the date starts at the local date, then run `mvn package` successfully
