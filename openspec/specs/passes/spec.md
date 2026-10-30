# passes Specification

## Purpose

Defines SpeedPasses and Special Passes: personal purchases attached to individual tickets, their identifiers, prices, refunds and consumed state.

## Requirements

### Requirement: Pass identity and state
Every pass SHALL have an identifier unique among all passes, a stored price, its individual ticket (bought alone or composing a group ticket) and a consumed flag. Consumed passes SHALL remain stored while their ticket exists.

#### Scenario: Pass on a composed ticket
- **WHEN** `SPEEDPASS CREATE;SP01;TG01:12345678Z;1` is processed for an existing future composed ticket
- **THEN** pass `SP01:1` exists for that ticket, unconsumed

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of its ticket's stored price and a Special Pass SHALL cost the ticket's stored price (discounted price for composed tickets). Later SYSTEM PRICE changes SHALL NOT affect them.

#### Scenario: Prices from ticket
- **WHEN** a 40.00 ticket and a 32.00 composed ticket each buy one SpeedPass and one Special Pass
- **THEN** the SpeedPasses cost 8.00 and 6.40 and the Special Passes cost 40.00 and 32.00

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create 1 or 10 independent SpeedPasses identified `<purchaseIdentifier>:1` … `:<quantity>` for an existing individual ticket dated on or after the current date, and print the total (quantity × unit price, no package discount). Any other quantity, or any identifier conflict, SHALL reject the whole purchase.

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket
- **THEN** passes SP02:1 to SP02:10 exist and the printed total is 80.00

#### Scenario: Invalid quantity or conflict
- **WHEN** the quantity is 5, or SP02:3 already exists and SP02 is bought again with quantity 10
- **THEN** an `ERROR:` message is printed and no pass is created

#### Scenario: Past or unknown ticket
- **WHEN** the ticket is dated before the current date, does not exist, or is a group ticket
- **THEN** an `ERROR:` message is printed and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current date and print its price. A ticket SHALL have at most one existing Special Pass, consumed or not.

#### Scenario: Second Special Pass rejected
- **WHEN** T001 already has a Special Pass (even consumed) and another is requested
- **THEN** an `ERROR:` message is printed

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete one pass of that type only when it is unconsumed and its ticket is dated strictly after the current date.

#### Scenario: Refund one pass of a package
- **WHEN** SP02:1…SP02:10 exist for a future ticket and `SPEEDPASS DELETE;SP02:4` is processed
- **THEN** only SP02:4 is removed

#### Scenario: Refund rejected
- **WHEN** the pass is consumed or its ticket is dated on or before the current date
- **THEN** an `ERROR:` message is printed and the pass is kept
