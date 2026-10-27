# Tasks

## 1. Singletons and injection

- [x] 1.1 Turn the user, group, ticket, attraction and invoice controllers into Singletons with `configure(...)` injection of repositories/collaborators, and make `ParkController` receive its repositories through `configure`; verify with `SingletonControllersTest` (same instance, no public constructors)
- [x] 1.2 Add `ApplicationInitializer` (permanent, temporary, in-memory) as the only place creating repositories; use it from `Main`; make `CommandProcessor` use the configured singletons; verify `mvn package` and the fly/permanent tests

## 2. Verification

- [x] 2.1 Update tests to initialise through `ApplicationInitializer` and add checks that repositories are not singletons and that a substitute repository is used; verify the full regression suite passes

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C03] Add singleton controllers and dependency injection`; verify with `git log -1 --format=%s`
