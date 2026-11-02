# Spec Delta

## Purpose

Lets users buy personal SpeedPasses and Special Passes attached to individual tickets, keeps them as purchase records once consumed, and allows refunds only while unused and still in the future.

## ADDED Requirements

### Requirement: Pass identity and pricing
Each pass SHALL have its own identifier, unique among all passes, a stored price, its associated individual ticket (standalone or a group constituent) and a consumed flag. A SpeedPass SHALL cost 20% and a Special Pass 100% of the associated ticket's stored (possibly discounted) price. (F3-C06)

#### Scenario: Prices from discounted ticket
- **WHEN** a pass is bought for a group constituent priced 32.00 after `SYSTEM PRICE` changed
- **THEN** a SpeedPass costs 6.40 and a Special Pass 32.00

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL, for quantity 1 or 10 and an existing individual ticket dated today or later, create that many independent SpeedPasses identified `<purchaseIdentifier>:1` … `:<quantity>` and display the total (quantity × unit price, no extra discount); if any identifier already exists nothing SHALL be created. (F3-C06)

#### Scenario: Ten-pack
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` runs for a 40.00 ticket
- **THEN** passes SP02:1 to SP02:10 exist and the displayed total is 80.00

#### Scenario: Rejected purchase
- **WHEN** quantity is not 1 or 10, the ticket does not exist, is a group ticket, is dated before today, or any generated identifier already exists
- **THEN** no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL, for an existing individual ticket dated today or later that has no Special Pass, create one Special Pass and display its price; a ticket SHALL NOT hold more than one Special Pass, consumed or not. (F3-C06)

#### Scenario: Second Special Pass
- **WHEN** a ticket already has a Special Pass (consumed or not) and another is requested
- **THEN** the purchase is rejected

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete a pass of the named kind only when it is not consumed and its ticket's date is strictly after the system date; consumed passes SHALL remain stored. (F3-C06)

#### Scenario: Refund rejected
- **WHEN** the pass is consumed or its ticket is dated today or earlier
- **THEN** the refund is rejected and the pass remains

#### Scenario: Refund one pass of a pack
- **WHEN** `SPEEDPASS DELETE;SP02:3` runs for an unused pass of a future ticket
- **THEN** only SP02:3 is removed

### Requirement: Cascade on ticket deletion
A valid `TICKET DELETE` SHALL delete all passes of that ticket, and a valid `GROUPTICKET DELETE` SHALL delete all passes of its constituent tickets, the constituents and the group ticket, all-or-nothing. (F3-C06, F3-C09)

#### Scenario: Group ticket cascade
- **WHEN** a future group ticket whose constituents have passes is deleted
- **THEN** the passes, constituents and group ticket are all gone, and if saving fails none of them is removed
