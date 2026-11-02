# Spec Delta

## Purpose

Provides one simulated current date and one runtime base ticket price for the whole application so that date- and price-dependent rules can be exercised deterministically.

## ADDED Requirements

### Requirement: Single runtime system date
The application SHALL hold exactly one current system date, initialized at startup to the operating system's current local date and not persisted. Every date-dependent business rule SHALL evaluate against this date and SHALL NOT consult the operating system date directly.

#### Scenario: Startup date
- **WHEN** the application starts
- **THEN** the system date equals the operating system's local date for that day

#### Scenario: Date not persisted
- **WHEN** the system date is changed and the application is restarted
- **THEN** the system date is again the operating system's local date

### Requirement: Set system date
`SYSTEM DATE;yyyy-MM-dd` SHALL set the system date when the value is a valid calendar date in that exact format, allowing moves forward or backward, and the new date SHALL apply to every subsequent command. An invalid value SHALL be rejected with an error and leave the date unchanged.

#### Scenario: Date updated for subsequent operations
- **WHEN** `SYSTEM DATE;2026-09-15` is executed
- **THEN** subsequent date-sensitive commands evaluate against 2026-09-15

#### Scenario: Moving backward
- **WHEN** `SYSTEM DATE;2026-10-20` and then `SYSTEM DATE;2026-10-01` are executed
- **THEN** the system date is 2026-10-01

#### Scenario: Invalid date
- **WHEN** `SYSTEM DATE;2026-13-01`, `SYSTEM DATE;2026-02-30`, `SYSTEM DATE;15/09/2026`, or `SYSTEM DATE;2026-9-15` is executed
- **THEN** an error is shown and the previous system date remains

### Requirement: Runtime base ticket price
The application SHALL hold a base individual-ticket price initialized to `40.00` on every run and not persisted. `SYSTEM PRICE;<price>` SHALL set it to a positive decimal amount with at most two decimal places; any other value SHALL be rejected without changing the price. Changing the price SHALL NOT alter already purchased tickets.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is executed and then a ticket is bought
- **THEN** the ticket is sold at `60.00`

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;0`, or `SYSTEM PRICE;10.555` is executed
- **THEN** an error is shown and the base price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price was changed and the application restarts
- **THEN** the base price is `40.00`
