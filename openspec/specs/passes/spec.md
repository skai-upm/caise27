# passes Specification

## Purpose
Lets visitors add personal passes to their individual tickets — SpeedPasses and one Special Pass — with prices derived from the ticket, consumption status, refunds and persistence.

## Requirements

### Requirement: Buy SpeedPasses
`SPEEDPASS CREATE;IdEntrada;Cantidad` SHALL accept only the quantities 1 and 10 and create that many independent SpeedPasses for the existing individual ticket, each with its own identifier `<IdEntrada>-SP<n>`, its own price equal to 20% of the ticket's stored price, and an unused status. The response SHALL show the amount charged.

#### Scenario: Pack of ten
- **WHEN** T001 cost 40.00 and `SPEEDPASS CREATE;T001;10` is processed
- **THEN** ten SpeedPasses of 8.00 each are created and the response shows 80.00 charged

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;T001;5` is processed
- **THEN** an error is reported and no pass is created

### Requirement: Buy the Special Pass
`SPECIALPASS CREATE;IdEntrada` SHALL create the ticket's Special Pass (identifier `<IdEntrada>-SPECIAL`) priced at the ticket's stored price, only if the ticket has no Special Pass, whether used or not. The response SHALL show the amount charged.

#### Scenario: First Special Pass
- **WHEN** T001 cost 40.00 and `SPECIALPASS CREATE;T001` is processed
- **THEN** the Special Pass is created and the response shows 40.00 charged

#### Scenario: Second Special Pass
- **WHEN** T001 already has a Special Pass (used or not) and `SPECIALPASS CREATE;T001` is processed
- **THEN** an error is reported

### Requirement: Passes require a usable individual ticket
Passes SHALL be bought only for an existing individual ticket (including tickets generated inside a group ticket) dated on or after the system date; group tickets themselves SHALL NOT receive passes.

#### Scenario: Group ticket identifier
- **WHEN** `SPEEDPASS CREATE;TG01;1` is processed for a group ticket
- **THEN** an error is reported

#### Scenario: Internal ticket
- **WHEN** `SPEEDPASS CREATE;TG01-1;1` is processed for an individual ticket inside group ticket TG01
- **THEN** the SpeedPass is created

#### Scenario: Past ticket
- **WHEN** the ticket's date is before the system date
- **THEN** the purchase is rejected

### Requirement: Consumption status
Each pass SHALL record whether it has been consumed. Consuming a pass SHALL mark it as consumed and SHALL NOT remove it from the system.

#### Scenario: Consumed pass kept
- **WHEN** a SpeedPass is consumed
- **THEN** it still exists with status consumed

### Requirement: Refund unused passes
`SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase` SHALL delete an unused pass of the corresponding kind and report the refunded amount only when the associated ticket's date is strictly after the system date. Consumed passes SHALL NOT be refunded.

#### Scenario: Refund before the ticket date
- **WHEN** the system date is before T001's date and `SPEEDPASS DELETE;T001-SP1` is processed for an unused pass
- **THEN** the pass is removed and the response shows 8.00 refunded

#### Scenario: Refund on the ticket date or later
- **WHEN** the system date is on or after T001's date
- **THEN** the refund is rejected

#### Scenario: Consumed pass
- **WHEN** the pass is consumed
- **THEN** the refund is rejected

### Requirement: Pass persistence
Passes SHALL be stored with their kind, identifier, associated individual ticket, price and consumed status and restored on start-up attached to their tickets, in normal and `--fly` modes. Passes removed by ticket deletions SHALL be removed from storage as well.

#### Scenario: Restart
- **WHEN** T001 has two SpeedPasses (one consumed) and a Special Pass and the application restarts
- **THEN** the three passes are restored with the same prices and statuses

#### Scenario: Fly mode
- **WHEN** passes are bought and consumed during a `--fly` run
- **THEN** they are stored only in the temporary directory and the permanent data is unchanged

#### Scenario: Cascade persisted
- **WHEN** a ticket with passes is deleted and the application restarts
- **THEN** none of its passes is restored

### Requirement: Pass commands in HELP
`HELP` and `HELP EXTENDED` SHALL list `SPEEDPASS CREATE;IdEntrada;Cantidad`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS CREATE;IdEntrada` and `SPECIALPASS DELETE;IdPase`.

#### Scenario: Help shows pass commands
- **WHEN** `HELP` is processed
- **THEN** the four syntaxes are listed

### Requirement: Passes consumed by attraction entry
SpeedPasses and Special Passes SHALL be consumed only by successful attraction entries, and the new consumed status SHALL be persisted immediately.

#### Scenario: Consumption survives a restart
- **WHEN** a SpeedPass is consumed by `ATTRACTION ENTER;A01;T001;SPEED` and the application restarts
- **THEN** that SpeedPass is restored as consumed
