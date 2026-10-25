# system-settings Specification

## Purpose

Provides the single simulated current date used by every time-dependent rule, and the current price of an individual ticket, both configurable through commands.

## Requirements

### Requirement: Single system date
The application SHALL keep exactly one current system date shared by all operations. At startup it SHALL be the operating system's current date; it SHALL NOT be persisted. No time-dependent business rule SHALL read the computer's real date directly.

#### Scenario: Startup date
- **WHEN** the application starts
- **THEN** the system date equals the operating system's current date

#### Scenario: Not persisted
- **WHEN** the system date is changed and the application is restarted
- **THEN** the system date is again the operating system's current date

### Requirement: Set system date
`SYSTEM DATE;yyyy-MM-dd` SHALL set the system date to any valid calendar date, earlier or later than the current one, and the new date SHALL apply immediately to subsequent operations. A value not in strict `yyyy-MM-dd` format or not a real calendar date SHALL be rejected and the date left unchanged.

#### Scenario: Valid date
- **WHEN** `SYSTEM DATE;2026-09-15` is executed
- **THEN** the system date becomes 2026-09-15 and a success message is shown

#### Scenario: Move backwards
- **WHEN** the system date is 2026-10-16 and `SYSTEM DATE;2026-10-01` is executed
- **THEN** the system date becomes 2026-10-01

#### Scenario: Invalid format
- **WHEN** any of `SYSTEM DATE;15-09-2026`, `SYSTEM DATE;2026-9-15`, `SYSTEM DATE;2026-02-30` or `SYSTEM DATE;hoy` is executed
- **THEN** an error is shown and the system date is unchanged

### Requirement: Ticket price
The current individual ticket price SHALL start at `40.00` on every run and SHALL NOT be persisted. `SYSTEM PRICE;<amount>` SHALL set it to a positive amount written as digits with an optional decimal point and at most two decimals; other values SHALL be rejected. The new price SHALL apply to tickets bought afterwards.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is executed and then an individual ticket is bought
- **THEN** the ticket costs `60.00`

#### Scenario: Invalid price
- **WHEN** any of `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc` or `SYSTEM PRICE;1.234` is executed
- **THEN** an error is shown and the price is unchanged
