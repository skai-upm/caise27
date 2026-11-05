# Spec Delta

## Purpose

Defines SpeedPasses and Special Passes: personal passes bought for an individual ticket, their identity, pricing, consumption state and refunds.

## ADDED Requirements

### Requirement: Pass identity and state
Each pass SHALL have its own identifier, its price, its associated individual ticket (which may be part of a group ticket) and a consumed indicator. Pass identifiers SHALL be unique among existing passes; this space is independent of ticket identifiers. Consumed passes SHALL remain in the system as purchase records.

#### Scenario: Consumed pass kept
- **GIVEN** a pass that has been consumed
- **WHEN** the passes of its ticket are inspected
- **THEN** the pass is still present, marked as consumed

### Requirement: Pass prices
Each pass price SHALL be computed from the price stored in its associated individual ticket: a SpeedPass SHALL cost 20% of it and a Special Pass SHALL cost the same. The ticket price already includes any group discount, and later `SYSTEM PRICE` changes SHALL NOT affect it.

#### Scenario: Prices from a 40.00 ticket
- **GIVEN** individual ticket `T001` priced 40.00
- **WHEN** a SpeedPass and a Special Pass are bought for it
- **THEN** they cost 8.00 and 40.00

#### Scenario: Discounted component
- **GIVEN** group-ticket component `TG01:12345678Z` priced 32.00
- **WHEN** a SpeedPass is bought for it
- **THEN** it costs 6.40

### Requirement: SpeedPass purchase
The command `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create `quantity` independent SpeedPasses identified `<purchaseIdentifier>:1` to `<purchaseIdentifier>:<quantity>` for an existing individual ticket dated on or after the current system date. The quantity SHALL be 1 or 10, with no package discount, and the total price charged SHALL be displayed. All identifiers SHALL be checked first; on any conflict or invalid input, nothing SHALL be created.

#### Scenario: Package of ten
- **GIVEN** ticket `T001` priced 40.00 and dated today
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` to `SP02:10` exist, each costing 8.00, and the total shown is 80.00

#### Scenario: Single pass
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists and 8.00 is shown

#### Scenario: Invalid quantity or ticket
- **WHEN** a purchase uses quantity `5`, an unknown ticket, a ticket dated before the current system date, or a group ticket identifier
- **THEN** an error is shown and no pass is created

#### Scenario: Identifier conflict
- **GIVEN** pass `SP03:4` exists
- **WHEN** `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** an error is shown and none of `SP03:1`..`SP03:10` except the existing `SP03:4` exists

### Requirement: Special Pass purchase
The command `SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current system date and SHALL display its price. An individual ticket SHALL have at most one Special Pass: a second purchase SHALL be rejected while the first exists, even if it was consumed.

#### Scenario: Special Pass bought
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for ticket `T001` priced 40.00
- **THEN** `PE01` exists and 40.00 is shown

#### Scenario: Second Special Pass rejected
- **GIVEN** `T001` already has Special Pass `PE01`, consumed or not
- **WHEN** `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is shown and `PE02` is not created

### Requirement: Pass refunds
The commands `SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL refund (delete) a single pass of that type only if it has not been consumed and its ticket is dated strictly after the current system date. Each pass of a package SHALL be refundable separately. Refunded passes SHALL leave the persisted state. Other refunds SHALL be rejected, leaving the pass unchanged.

#### Scenario: Refund one pass of a package
- **GIVEN** future ticket `T001` with passes `SP02:1`..`SP02:10`
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** only `SP02:3` is removed

#### Scenario: Consumed or current-day pass not refundable
- **WHEN** a refund is requested for a consumed pass, or for a pass whose ticket is dated today or earlier
- **THEN** an error is shown and the pass remains

#### Scenario: Wrong pass type or unknown pass
- **WHEN** `SPECIALPASS DELETE` names a SpeedPass, or an identifier that does not exist
- **THEN** an error is shown
