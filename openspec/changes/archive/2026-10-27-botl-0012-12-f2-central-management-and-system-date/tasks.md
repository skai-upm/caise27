# Tasks

## 1. Central controller

- [x] 1.1 Create `park/ParkController` singleton (private constructor, `getInstance`, `start(UserRepository)`, users/date/price accessors); verify with `ParkControllerTest` (same instance, no public constructor, initial date = today, initial price = 40.00 after restart)
- [x] 1.2 Route `CommandProcessor` user operations through the controller and initialise it in `Main.run`; verify all existing tests still pass

## 2. SYSTEM commands

- [x] 2.1 Implement `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` with strict validation; verify each park-management scenario in `CommandProcessorTest`, and update HELP to list both commands

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F2-C01] Add park controller and system date`; verify with `git log -1 --format=%s`
