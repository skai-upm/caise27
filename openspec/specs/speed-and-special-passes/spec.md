# speed-and-special-passes Specification

## Purpose

Lets users buy SpeedPasses and Special Passes attached to individual tickets, priced from the ticket's stored price, and refund unused passes for future tickets.

## Requirements

### Requirement: Pass identity and state
Each pass SHALL have its own identifier, price, associated individual ticket (standalone or part of a group ticket's composition) and a consumed indicator. Pass identifiers SHALL be unique among passes; they need not be distinct from ticket identifiers. Consumed passes SHALL remain stored as purchase records.

#### Scenario: Consumed pass kept
- **WHEN** a pass is consumed by an attraction entry
- **THEN** it still exists, marked as consumed

### Requirement: Pass pricing
A SpeedPass SHALL cost 20% of the stored price of its individual ticket; a Special Pass SHALL cost that stored price. For a ticket composing a group ticket, the discounted ticket price SHALL be used. Later `SYSTEM PRICE` changes SHALL NOT affect them.

#### Scenario: Prices from the ticket
- **WHEN** T001 cost 40.00 and the base price is then changed to 50.00
- **THEN** a SpeedPass for T001 costs 8.00 and a Special Pass for T001 costs 40.00

#### Scenario: Discounted group ticket
- **WHEN** a composition ticket of a 3-member group ticket cost 36.00
- **THEN** a SpeedPass for it costs 7.20

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL accept quantity 1 or 10 only, require an existing individual ticket dated on or after the current system date, and create that many independent SpeedPasses identified `<purchaseIdentifier>:1` to `<purchaseIdentifier>:<quantity>`. All identifiers SHALL be checked before creating any pass; a conflict SHALL reject the whole purchase. The total price (quantity × unit price, no extra discount) SHALL be displayed.

#### Scenario: Ten-pack
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket valid today
- **THEN** passes `SP02:1` to `SP02:10` exist and a total of 80.00 is displayed

#### Scenario: Single pass identifier
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists

#### Scenario: Rejected purchase
- **WHEN** the quantity is 5, the ticket does not exist, the ticket is a group ticket, the ticket date is in the past, or `SP02:3` already exists
- **THEN** the purchase is rejected and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current system date, provided the ticket has no Special Pass (consumed or not), and SHALL display the price charged.

#### Scenario: Second Special Pass rejected
- **WHEN** T001 already has a Special Pass, even a consumed one, and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** the purchase is rejected

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete an unconsumed pass of that type whose individual ticket is dated strictly after the current system date; each pass of a package SHALL be refundable separately. A refunded pass SHALL no longer be part of the persisted state.

#### Scenario: Refund of a future unused pass
- **WHEN** SP02:3 belongs to a ticket dated tomorrow, is unused, and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** SP02:3 no longer exists and SP02:1 is unaffected

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, or its ticket is dated today or earlier
- **THEN** the refund is rejected and the pass remains stored
