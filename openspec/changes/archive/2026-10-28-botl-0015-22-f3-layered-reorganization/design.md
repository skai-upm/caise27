# Design

## Context

Current packages: `cli` (commands, processor), `controller` (`ParkController`), `model`, `persistence`, root `App`. Errors are signalled with `IllegalArgumentException`/`IllegalStateException`; persistence uses `UncheckedIOException`; presentation catches those two generic types.

## Goals / Non-Goals

**Goals:** layered packages, typed exception hierarchy, unchanged behaviour.

**Non-Goals:** new commands, Builder/DI patterns (F3-C02/F3-C03).

## Decisions

- **Package mapping**: `cli` → `presentation`; `controller` → `business.controller`; `model` → `business.model`; new `business.exception`; `persistence` unchanged. Done with `git mv` + package/import rewrite so history is kept.
- **Exception hierarchy** (`business.exception`): `ParkException extends RuntimeException` (abstract base); `NotFoundException` (unknown user/group/ticket/attraction, non-member, not inside); `AlreadyExistsException` (duplicate identifiers/e-mails/members); `InvalidValueException` (malformed date/price/capacity); `OperationNotAllowedException` (business rule violations: locked group, active tickets, past dates, non-deletable tickets, capacity, wrong ticket date, empty group, composed ticket deletion). `persistence.PersistenceException extends ParkException` for I/O failures. Messages stay user-facing, so output text is unchanged.
- **Handling level**: commands (`ControllerCommand`, SYSTEM commands) catch `ParkException` and print `ERROR: <message>`; `CommandProcessor` keeps a last-resort `RuntimeException` handler; `App` catches `PersistenceException` around load/save; `ParkStorage` catches `ParkException` per stored record.
- **Architecture test**: scans source files' `import` lines to assert presentation does not import `persistence` and business does not import `persistence`/`presentation`.
- **Tests**: generic exception assertions updated to the new types (`ParkException` or the specific subtype).
