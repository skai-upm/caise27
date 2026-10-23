# Spec Delta

## Purpose

Defines SpeedPasses and Special Passes: personal add-ons bought for an individual ticket, their prices, identifiers, consumption state, refunds and deletion together with their ticket.

## ADDED Requirements

### Requirement: Pass identity and state
Every pass SHALL have an identifier unique among all existing passes (SpeedPasses and Special Passes share this space, separate from ticket identifiers), a stored price, its individual ticket, and a consumed flag that starts false; consumed passes SHALL remain stored while their ticket exists.

#### Scenario: Duplicate pass identifier
- **WHEN** pass `PE01` exists and `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an error is printed and no pass is created

### Requirement: Pass pricing
A SpeedPass SHALL cost 20% of the stored price of its individual ticket and a Special Pass SHALL cost the stored price of its individual ticket (the discounted price for tickets composing a group ticket); later `SYSTEM PRICE` changes SHALL NOT affect pass prices.

#### Scenario: Prices from a standalone ticket
- **WHEN** T001 cost 40.00 and the base price is later changed to 45.50
- **THEN** a SpeedPass for T001 costs 8.00 and a Special Pass for T001 costs 40.00

#### Scenario: Prices from a group component
- **WHEN** `TG01:12345678Z` cost 36.00 (10% group discount)
- **THEN** a SpeedPass for it costs 7.20

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL, for an existing individual ticket (standalone or composing a group ticket) dated on or after the current system date and a quantity of exactly 1 or 10, create that many independent SpeedPasses identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>`, checking all identifiers first so nothing is created on conflict, and print the total price (quantity × unit price, no package discount).

#### Scenario: Package of ten
- **WHEN** T001 cost 40.00 and `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` to `SP02:10` exist and the output reports a total of 80.00

#### Scenario: Single pass
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists

#### Scenario: Invalid quantity or ticket
- **WHEN** the quantity is not 1 or 10, the ticket does not exist, is a group ticket, or is dated before today
- **THEN** an error is printed and no pass is created

#### Scenario: Identifier conflict
- **WHEN** pass `SP03:4` already exists and `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** an error is printed and none of `SP03:1`…`SP03:10` is created by this command

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current system date when that ticket has no Special Pass (consumed or not), printing its price.

#### Scenario: Buy special pass
- **WHEN** T001 cost 40.00 and `SPECIALPASS CREATE;PE01;T001` is processed
- **THEN** PE01 exists for T001 and the output reports a price of 40.00

#### Scenario: Second special pass
- **WHEN** T001 already has a Special Pass, consumed or not, and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is printed

### Requirement: Pass refund
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete a pass of the corresponding type only when it is not consumed and its individual ticket is dated strictly after the current system date; each SpeedPass of a package is refunded individually.

#### Scenario: Refund one pass of a package
- **WHEN** T002 is dated after today and `SPEEDPASS DELETE;SP05:3` is processed
- **THEN** `SP05:3` no longer exists and `SP05:1` still exists

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, its ticket is dated today or earlier, the identifier is unknown, or the pass is of the other type
- **THEN** an error is printed and the pass is kept
