# Spec Delta

## Purpose

Defines the single simulated current date of the application, how it is initialised and changed with `SYSTEM DATE`, and the rule that every time-dependent operation uses it instead of the computer's real date.

## ADDED Requirements

### Requirement: Set system date
The command `SYSTEM DATE;yyyy-MM-dd` SHALL set the application's current date to the given date and print a confirmation starting with `OK` that includes the new date. The date MAY be moved backward or forward any number of times.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is processed
- **THEN** the current date becomes 2026-09-15 and `OK SYSTEM DATE SET -> 2026-09-15` is printed

#### Scenario: Moving backward
- **WHEN** the current date is 2026-09-15 and `SYSTEM DATE;2025-01-01` is processed
- **THEN** the current date becomes 2025-01-01

### Requirement: Date format validation
The date parameter SHALL be a real calendar date written exactly as four-digit year, two-digit month and two-digit day separated by `-`. Any other value SHALL be rejected with an `ERROR` line and the current date SHALL remain unchanged.

#### Scenario: Wrong format
- **WHEN** `SYSTEM DATE;15/09/2026` or `SYSTEM DATE;2026-9-15` is processed
- **THEN** an `ERROR` line is printed and the current date is unchanged

#### Scenario: Impossible date
- **WHEN** `SYSTEM DATE;2026-02-30` is processed
- **THEN** an `ERROR` line is printed and the current date is unchanged

#### Scenario: Missing parameter
- **WHEN** `SYSTEM DATE` is processed without a date
- **THEN** an `ERROR` line about the wrong number of parameters is printed

### Requirement: Single current date
There SHALL be exactly one current date for the whole application, held by the main controller. Every time-dependent operation SHALL obtain the date from it, so a change takes effect immediately for all subsequent operations, and no time-dependent business rule SHALL consult the computer's real date directly.

#### Scenario: Change visible to subsequent operations
- **WHEN** `SYSTEM DATE;2026-10-16` is processed
- **THEN** any operation executed afterwards evaluates its date rules against 2026-10-16

### Requirement: Initial system date
At the start of each execution the current date SHALL be the operating system's current date. The current date SHALL NOT be persisted between executions.

#### Scenario: Fresh execution
- **WHEN** the application starts, regardless of any date set in a previous execution
- **THEN** the current date equals the operating system's date at startup
