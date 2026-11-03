# Spec Delta

## Purpose

Adds SpeedPasses and Special Passes as personal purchases attached to individual tickets, with their own identity, pricing, consumed state and refunds (source F3-C06, sections 9.1-9.4).

## ADDED Requirements

### Requirement: Pass identity and state
Each pass SHALL have an identifier unique among existing passes, a price, an associated individual ticket (standalone or inside a group ticket) and a consumed indicator; consumed passes SHALL remain stored as purchase records.

#### Scenario: Pass attached to a composed ticket
- **WHEN** `SPEEDPASS CREATE;SP01;TG01:12345678Z;1` is processed for a future group ticket `TG01`
- **THEN** pass `SP01:1` is associated with individual ticket `TG01:12345678Z` and is unconsumed

### Requirement: Pass prices
A SpeedPass SHALL cost 20 % of the price stored in its individual ticket and a Special Pass SHALL cost that price, so later `SYSTEM PRICE` changes do not affect them and group discounts carry over.

#### Scenario: Prices from a discounted composed ticket
- **WHEN** a composed ticket priced 36.00 (10 % group discount) gets a SpeedPass and a Special Pass
- **THEN** their prices are 7.20 and 36.00

### Requirement: Buy SpeedPasses
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL, for quantity `1` or `10` and an existing individual ticket dated on or after the current date, create that many independent passes identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>` and display the total (quantity × unit price, no package discount); if any identifier is already used, nothing SHALL be created.

#### Scenario: Ten-pack
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket
- **THEN** passes `SP02:1` … `SP02:10` exist and total 80.00 is displayed

#### Scenario: Invalid quantity, past ticket or group ticket
- **WHEN** quantity is `5`, the ticket is dated before the current date, or the identifier names a group ticket
- **THEN** an error is reported and no pass is created

#### Scenario: Identifier conflict
- **WHEN** pass `SP03:2` exists and `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** an error is reported and none of the ten passes is created

### Requirement: Buy a Special Pass
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create one Special Pass for an existing individual ticket dated on or after the current date and display its price; it SHALL be rejected if the ticket already has a Special Pass (consumed or not) or the identifier is used.

#### Scenario: Second Special Pass rejected
- **WHEN** `T001` has Special Pass `PE01` (even if consumed) and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is reported and `PE02` is not created

### Requirement: Refund passes
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete a pass of the matching type only when it is unconsumed and its ticket is dated strictly after the current date; each pass of a package SHALL be refundable individually.

#### Scenario: Refund one pass of a package
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed for an unconsumed pass of a future ticket
- **THEN** only `SP02:3` is removed

#### Scenario: Consumed or current-day pass not refundable
- **WHEN** the pass is consumed or its ticket is dated today
- **THEN** an error is reported and the pass remains stored
