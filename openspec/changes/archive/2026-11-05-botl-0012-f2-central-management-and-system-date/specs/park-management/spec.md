# Spec Delta

## Purpose

Defines the single central park management object that coordinates the overall park state during an execution, including the current system date and the current base ticket price, and the commands that change them.

## ADDED Requirements

### Requirement: Single central park management instance
The system SHALL have exactly one central park management object per execution; every component requesting central park management SHALL obtain that same instance, and it SHALL give access to the park's users.

#### Scenario: Same instance everywhere
- **WHEN** central park management is requested twice from different components during one execution
- **THEN** both requests return the same instance

#### Scenario: Users accessed centrally
- **WHEN** a user is created with `USER CREATE`
- **THEN** that user is reachable through the central park management object

### Requirement: Initial system date and price
At the start of every execution the current system date SHALL be the local date at which execution begins and the current base ticket price SHALL be 40.00 euros, regardless of values set in previous executions.

#### Scenario: Initial values
- **WHEN** the application starts
- **THEN** the system date equals the machine's local date and the base price is `40.00`

#### Scenario: Price not carried over
- **WHEN** `SYSTEM PRICE;45.50` is processed, the application exits and is started again
- **THEN** the base price is `40.00`

### Requirement: SYSTEM DATE command
The command `SYSTEM DATE;<yyyy-MM-dd>` SHALL set the current system date to the given valid calendar date and print a response starting with `OK`; from then on date-dependent operations SHALL use that date. It MAY be used any number of times. A missing, malformed or non-existent date MUST be rejected with an `ERROR` response, leaving the date unchanged.

#### Scenario: Date replaced
- **WHEN** `SYSTEM DATE;2027-04-15` is processed
- **THEN** a response starting with `OK` is printed and the current system date is 2027-04-15

#### Scenario: Date changed repeatedly
- **WHEN** `SYSTEM DATE;2027-04-15` and then `SYSTEM DATE;2027-05-01` are processed
- **THEN** the current system date is 2027-05-01

#### Scenario: Invalid date rejected
- **WHEN** `SYSTEM DATE;2027-02-30`, `SYSTEM DATE;15/04/2027`, `SYSTEM DATE;2027-4-15` or `SYSTEM DATE` is processed
- **THEN** an `ERROR` response is printed and the current system date is unchanged

### Requirement: SYSTEM PRICE command
The command `SYSTEM PRICE;<Price>` SHALL set the current base ticket price to the given amount when it is a valid, positive monetary amount (digits with an optional decimal point and at most two decimals) and print a response starting with `OK`. Any other value MUST be rejected with an `ERROR` response, leaving the price unchanged. The base price applies to tickets created after the change and does not alter tickets already created.

#### Scenario: Price replaced
- **WHEN** `SYSTEM PRICE;45.50` is processed
- **THEN** a response starting with `OK` is printed and the base price is `45.50`

#### Scenario: Invalid prices rejected
- **WHEN** `SYSTEM PRICE;0`, `SYSTEM PRICE;-5`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;45.555`, `SYSTEM PRICE;1E2` or `SYSTEM PRICE` is processed
- **THEN** an `ERROR` response is printed and the base price is unchanged

### Requirement: System commands listed in HELP
The `HELP` output SHALL also list `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<price>`.

#### Scenario: HELP shows system commands
- **WHEN** `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<price>`
