# personal-passes Specification

## Purpose


Defines personal passes attached to individual tickets — SpeedPasses and Special Passes — including purchase rules and prices, their consumption status, refunds, and persistence.

## Requirements

### Requirement: Passes belong to individual tickets
Every pass SHALL be associated with exactly one individual ticket (standalone or internal to a group ticket) and SHALL have its own identifier, the price charged and a consumption status. An individual ticket MAY have any number of SpeedPasses and at most one Special Pass. Passes cannot be attached to group tickets as a whole.

#### Scenario: Pass for a group ticket rejected
- **WHEN** `SPEEDPASS CREATE;TG01;1` names a group ticket
- **THEN** an `ERROR` line states that passes belong to individual tickets

#### Scenario: Pass for an internal ticket
- **WHEN** `SPEEDPASS CREATE;TG01-1;1` names an individual ticket inside group ticket `TG01`
- **THEN** the SpeedPass is created for that individual ticket

### Requirement: Buy SpeedPasses
The command `SPEEDPASS CREATE;<ticketId>;<quantity>` SHALL accept quantities 1 and 10 only, and SHALL create that many independent SpeedPasses for the ticket, each priced at 20% of the price stored in the ticket (rounded to cents) and not consumed. It SHALL print `OK SPEEDPASS CREATED -> <amount charged>` followed by the new pass identifiers. The ticket must exist and be dated on or after the system date.

#### Scenario: One SpeedPass
- **WHEN** ticket `T001` was bought at 40.00 and `SPEEDPASS CREATE;T001;1` is processed
- **THEN** one SpeedPass priced 8.00 is created and the output starts with `OK SPEEDPASS CREATED -> 8.00`

#### Scenario: Pack of ten
- **WHEN** `SPEEDPASS CREATE;T001;10` is processed for a ticket priced 40.00
- **THEN** ten distinct SpeedPasses each priced 8.00 and unconsumed are created and 80.00 is charged

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;T001;5` is processed
- **THEN** no pass is created and an `ERROR` line is printed

#### Scenario: Past ticket
- **WHEN** the system date is after the date of `T001`
- **THEN** `SPEEDPASS CREATE;T001;1` is rejected with an `ERROR` line

### Requirement: Buy the Special Pass
The command `SPECIALPASS CREATE;<ticketId>` SHALL create the ticket's Special Pass priced at the price stored in the ticket and print `OK SPECIALPASS CREATED -> <price>` with its identifier. It SHALL be rejected if the ticket already has a Special Pass, whether used or not. The ticket must exist and be dated on or after the system date.

#### Scenario: Special Pass bought
- **WHEN** `SPECIALPASS CREATE;T001` is processed for a ticket priced 40.00
- **THEN** a Special Pass priced 40.00 is created

#### Scenario: Second Special Pass
- **WHEN** `T001` already has a Special Pass (consumed or not) and `SPECIALPASS CREATE;T001` is processed
- **THEN** an `ERROR` line is printed and no pass is created

### Requirement: Refund unused passes
The commands `SPEEDPASS DELETE;<passId>` and `SPECIALPASS DELETE;<passId>` SHALL remove an unused pass of the named type and print `OK SPEEDPASS REFUNDED -> <price>` or `OK SPECIALPASS REFUNDED -> <price>`, only when its ticket is dated strictly after the system date (the Delivery 2 refund rule). A consumed pass, a pass whose ticket date is today or past, a pass of the other type, or an unknown identifier SHALL be rejected with an `ERROR` line.

#### Scenario: Refund before the ticket date
- **WHEN** the system date is before the date of `T001` and its unused SpeedPass `SP1` is refunded
- **THEN** `SP1` is removed and `OK SPEEDPASS REFUNDED -> 8.00` is printed

#### Scenario: Refund on the ticket date
- **WHEN** the system date equals the date of `T001`
- **THEN** refunding its passes is rejected

#### Scenario: Consumed pass
- **WHEN** a consumed pass is refunded
- **THEN** an `ERROR` line is printed and the pass remains

### Requirement: Consumed passes are kept
Consuming a pass SHALL mark it as consumed without removing it, so it remains associated with its ticket with its price and status.

#### Scenario: Consumption keeps the pass
- **WHEN** a SpeedPass is consumed
- **THEN** it still exists, is marked as consumed, and cannot be consumed or refunded again

### Requirement: Pass persistence
Passes SHALL be stored with identifier, type, associated individual ticket, price and consumption status, and restored at startup attached to their tickets with the same price and consumption status. Pass identifiers SHALL stay unique across executions. In `--fly` executions passes SHALL be stored only in the run's temporary directory, like every other persistent element.

#### Scenario: Passes survive restart
- **WHEN** a SpeedPass and a Special Pass are bought, one is consumed, and the application restarts
- **THEN** both passes exist with the same ticket, price and consumption status, and new passes get identifiers not used before

#### Scenario: Passes of internal tickets
- **WHEN** a pass bought for internal ticket `TG01-1` is consumed and the application restarts
- **THEN** it is restored attached to `TG01-1` inside `TG01`, still consumed

#### Scenario: Fly mode
- **WHEN** passes are bought and consumed during a `--fly` run
- **THEN** the permanent passes file is not modified
