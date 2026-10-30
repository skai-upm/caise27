# passes Specification

## Purpose

Defines SpeedPasses and Special Passes bought for individual tickets: their pricing, purchase, refund, consumption state and deletion together with their ticket.

## Requirements

### Requirement: Pass identity and state
Every pass SHALL have an identifier unique among all passes, a stored price, its associated individual ticket (stand-alone or inside a group ticket) and a consumed flag. Consumed passes SHALL remain in the system as purchase records.

#### Scenario: Duplicate pass identifier
- **WHEN** pass `SP01:1` exists and a purchase would create `SP01:1` again
- **THEN** an error is printed and nothing is created

### Requirement: Pass pricing
A SpeedPass SHALL cost 20% of its individual ticket's stored price and a Special Pass SHALL cost that price, including any group discount. Later `SYSTEM PRICE` changes SHALL NOT affect them.

#### Scenario: Prices from ticket
- **WHEN** ticket T001 cost 40.00 and passes are bought after `SYSTEM PRICE;50`
- **THEN** a SpeedPass costs 8.00 and a Special Pass costs 40.00

#### Scenario: Discounted group member ticket
- **WHEN** ticket `TG02:12345678Z` cost 32.00 within a 6-member group ticket
- **THEN** a SpeedPass for it costs 6.40

### Requirement: Buy SpeedPasses
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL, for quantity 1 or 10 and an existing individual ticket dated today or later, create that many SpeedPasses identified `<purchaseIdentifier>:1` to `:<quantity>` and print the total price (quantity × unit price). Other quantities, group-ticket identifiers, past tickets or any identifier conflict SHALL reject the whole purchase.

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket
- **THEN** passes `SP02:1` … `SP02:10` exist and a total of 80.00 euros is printed

#### Scenario: Single pass
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;SP03;T001;5` is processed
- **THEN** an error is printed and no pass is created

#### Scenario: Partial conflict
- **WHEN** `SP04:3` already exists and `SPEEDPASS CREATE;SP04;T001;10` is processed
- **THEN** an error is printed and none of `SP04:1` … `SP04:10` is created by this command

### Requirement: Buy Special Pass
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated today or later that has no Special Pass (consumed or not), printing the price.

#### Scenario: Buy special pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for a 40.00 ticket dated today
- **THEN** PE01 exists unconsumed and 40.00 euros is printed

#### Scenario: Second special pass rejected
- **WHEN** T001 already has a Special Pass, consumed or not, and another is requested
- **THEN** an error is printed and no pass is created

### Requirement: Refund passes
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete an unconsumed pass of that kind whose ticket is dated strictly after the current system date. Consumed passes, passes of tickets dated today or earlier, unknown identifiers and the wrong pass kind SHALL be rejected.

#### Scenario: Refund one pass of a package
- **WHEN** T001 is dated tomorrow and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** only `SP02:3` is removed

#### Scenario: Pass of today's ticket kept
- **WHEN** T001 is dated today and `SPECIALPASS DELETE;PE01` is processed
- **THEN** an error is printed and PE01 still exists

### Requirement: Cascade with ticket deletion
A valid `TICKET DELETE` SHALL also delete all passes of that ticket, and a valid `GROUPTICKET DELETE` SHALL delete all passes of its composition tickets, then the composition, then the group ticket, with no partial deletion.

#### Scenario: Ticket refund removes passes
- **WHEN** future ticket T001 has passes from several purchases and `TICKET DELETE;T001` is processed
- **THEN** T001 and all its passes no longer exist
