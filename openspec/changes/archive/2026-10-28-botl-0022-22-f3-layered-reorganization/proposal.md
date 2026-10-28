# Proposal

## Why

Milestone F3-C01 starts Delivery 3 by reorganizing the code into clearly separated presentation, business and persistence layers (distinct Java packages) without changing the behavior reached at the end of Delivery 2, and by using exceptions as a designed mechanism to represent and propagate abnormal situations, handled at the right level.

## What Changes

- Move classes into layered packages under `es.upm.amusementpark`:
  - `presentation`: `App` (entry point, argument handling, input loop) and `CommandProcessor` (command parsing and responses).
  - `business`: `ParkController` (application control), `business.model` (domain), `business.service` (business rules), `business.exception` (exception hierarchy).
  - `persistence`: `persistence.repository` (in-memory repositories) and `persistence.file` (text-file storage, data locations, temporary `--fly` directory).
- Presentation no longer touches persistence files or directories: the temporary directory lifecycle moves to the persistence layer and is driven through `ParkController`.
- Replace the single generic `OperationException` by a hierarchy rooted at `ParkException`: `ValidationException` (invalid data), `NotFoundException` (unknown user/group/ticket/attraction), `ConflictException` (duplicates/uniqueness), `OperationNotAllowedException` (rule forbids the operation: locks, dates, capacity, wrong password), `DataAccessException` (persistence failure); presentation adds `CommandSyntaxException` (unknown command, argument count, malformed values). Business operations throw them; the presentation layer turns any `ParkException` into one `ERROR:` line and keeps processing.
- Update the manifest main class and `uml/model.puml` to the new packages.
- Commit with the mandatory message `[F3-C01] Reorganize application into layered packages`.

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering (presentation/business/persistence), dependency rules between layers, and the exception design.

### Modified Capabilities
- None (all observable behavior of Deliveries 1 and 2 is preserved).

## Impact

- Every source file moves package; `pom.xml` main class becomes `es.upm.amusementpark.presentation.App`; tests move alongside.
- Ambiguity flagged: the statement lets students choose between a central hierarchy and distributed exceptions; a central hierarchy in `business.exception` is chosen, with one presentation-specific subclass for command syntax.
