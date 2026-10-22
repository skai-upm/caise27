# passes Specification

## Purpose

Defines the personal passes that can be bought for an individual ticket — SpeedPasses and Special Passes — with their prices, identifiers, consumption state and refunds.

## Requirements

### Requirement: Pass identity and state
Every pass SHALL have a unique identifier among existing passes (SpeedPasses and Special Passes share this space, separate from tickets), a price fixed at purchase, the individual ticket it belongs to, and a consumed flag that starts as false. Consumed passes SHALL remain stored while their ticket exists.

#### Scenario: Duplicate pass identifier
- **WHEN** `SPECIALPASS CREATE;SP01:1;T002` is processed and a pass `SP01:1` exists
- **THEN** an error is printed and no pass is created

### Requirement: Pass eligibility
A pass SHALL only be bought for an existing individual ticket (stand-alone or part of a group ticket) whose date is on or after the system date. A group ticket identifier SHALL be rejected.

#### Scenario: Pass for a group-ticket component
- **WHEN** `SPEEDPASS CREATE;SP02;TG01:12345678Z;1` is processed and TG01 is dated on the system date
- **THEN** SpeedPass `SP02:1` is created for that individual ticket

#### Scenario: Historical ticket
- **WHEN** a pass is requested for an individual ticket dated before the system date
- **THEN** an error is printed and no pass is created

#### Scenario: Group ticket identifier
- **WHEN** `SPEEDPASS CREATE;SP05;TG01;1` is processed
- **THEN** an error is printed and no pass is created

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL accept a quantity of 1 or 10 and create that many SpeedPasses identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>`, each costing 20 % of the ticket's stored price. All identifiers SHALL be checked first; on any conflict nothing is created. The total charged SHALL be printed (no package discount).

#### Scenario: Package of ten
- **WHEN** T001 costs 40.00 and `SPEEDPASS CREATE;SP01;T001;10` is processed
- **THEN** passes `SP01:1` … `SP01:10` exist, each costing 8.00, and a total of 80.00 is printed

#### Scenario: Discounted component ticket
- **WHEN** a group-ticket component costs 36.00 and one SpeedPass is bought for it
- **THEN** the SpeedPass costs 7.20

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;SP09;T001;5` is processed
- **THEN** an error is printed and no pass is created

#### Scenario: Partial conflict
- **WHEN** `SPEEDPASS CREATE;SP01;T001;10` is processed and only `SP01:7` already exists
- **THEN** an error is printed and none of the ten passes is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass costing the ticket's stored price and print it. An individual ticket SHALL have at most one Special Pass, consumed or not.

#### Scenario: Buy a Special Pass
- **WHEN** T001 costs 40.00 and `SPECIALPASS CREATE;PE01;T001` is processed
- **THEN** PE01 exists for T001 and a price of 40.00 is printed

#### Scenario: Second Special Pass
- **WHEN** T001 already has a Special Pass (consumed or not) and another is requested
- **THEN** an error is printed and no pass is created

### Requirement: Pass refund
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL remove a pass of the matching kind only when it is unconsumed and its ticket's date is strictly later than the system date. Each SpeedPass of a package SHALL be refundable on its own.

#### Scenario: Refund one pass of a package
- **WHEN** T002 is dated after the system date and `SPEEDPASS DELETE;SP07:3` is processed
- **THEN** only `SP07:3` is removed

#### Scenario: Consumed pass
- **WHEN** a refund is requested for a consumed pass
- **THEN** an error is printed and the pass is kept

#### Scenario: Ticket for today
- **WHEN** a refund is requested for a pass whose ticket is dated on the system date
- **THEN** an error is printed and the pass is kept

#### Scenario: Wrong kind
- **WHEN** `SPECIALPASS DELETE` names a SpeedPass
- **THEN** an error is printed and the pass is kept
