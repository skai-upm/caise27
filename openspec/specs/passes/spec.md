# passes Specification

## Purpose

Defines SpeedPasses and Special Passes: personal extras bought for an individual ticket, with their prices, consumption state, refunds and deletion together with their ticket.

## Requirements

### Requirement: Pass identity and state
Each pass SHALL have its own identifier (unique among all passes), its price, its associated individual ticket and a consumed flag. Consumed passes SHALL remain stored while their ticket exists.

#### Scenario: Duplicate pass identifier
- **WHEN** pass `PE01` exists and `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an error is reported and no pass is created

### Requirement: Pass prices
A SpeedPass SHALL cost 20 % of its ticket's stored price and a Special Pass SHALL cost the ticket's stored price (discounted price for composition tickets). Later SYSTEM PRICE changes SHALL NOT affect them.

#### Scenario: Prices from ticket price
- **WHEN** `T001` cost 40.00, then `SYSTEM PRICE;50` is processed, then a SpeedPass and a Special Pass are bought for `T001`
- **THEN** the SpeedPass costs 8.00 and the Special Pass costs 40.00

#### Scenario: Discounted composition ticket
- **WHEN** a composition ticket cost 32.00 (20 % group discount) and a SpeedPass is bought for it
- **THEN** the SpeedPass costs 6.40

### Requirement: Buy SpeedPasses
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL, for an existing individual ticket dated on or after today and quantity `1` or `10`, create that many SpeedPasses identified `<purchaseIdentifier>:1` … `:<quantity>`, checking every identifier first, and display the total charged (quantity × unit price).

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket valid today
- **THEN** passes `SP02:1` … `SP02:10` exist and the output shows a total of 80.00

#### Scenario: Single pass naming
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists

#### Scenario: Invalid quantity, ticket or date
- **WHEN** the quantity is not 1 or 10, the ticket does not exist or is a group ticket, or the ticket date is before today
- **THEN** an error is reported and no pass is created

#### Scenario: Identifier conflict is atomic
- **WHEN** `SP03:5` already exists and `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** an error is reported and none of `SP03:1` … `SP03:10` is created by this command

### Requirement: Buy Special Pass
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after today that has no Special Pass, and display its price.

#### Scenario: Buy special pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for a 40.00 ticket valid today
- **THEN** `PE01` exists for `T001` and the output shows 40.00

#### Scenario: Second special pass rejected even after consumption
- **WHEN** `T001` already has a Special Pass, consumed or not, and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is reported

### Requirement: Refund passes
`SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` SHALL delete a pass of the named kind only if it is unconsumed and its ticket is dated strictly after today; the pass then disappears from the persisted state. Each SpeedPass of a package is refunded separately.

#### Scenario: Refund one pass of a package
- **WHEN** `SP02:1` … `SP02:10` exist for a ticket dated tomorrow and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** only `SP02:3` is deleted

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, or its ticket is dated today or earlier, or the identifier names a pass of the other kind
- **THEN** an error is reported and the pass still exists

### Requirement: Passes deleted with their ticket
A valid `TICKET DELETE` SHALL also delete all passes of that ticket, and a valid `GROUPTICKET DELETE` SHALL delete all passes of its composition tickets, the composition tickets and the group ticket, as one consistent operation. Rejected ticket deletions SHALL leave the passes untouched.

#### Scenario: Ticket deletion cascades
- **WHEN** future ticket `T001` has SpeedPasses from two purchases and a Special Pass and `TICKET DELETE;T001` is processed
- **THEN** `T001` and all its passes no longer exist

#### Scenario: Group ticket deletion cascades
- **WHEN** future group ticket `TG01` has composition tickets with passes and `GROUPTICKET DELETE;TG01` is processed
- **THEN** the passes, the composition tickets and `TG01` no longer exist
