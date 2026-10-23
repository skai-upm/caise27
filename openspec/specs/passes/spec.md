# passes Specification

## Purpose

Personal paid extras bought for a specific individual ticket: SpeedPasses for faster entry and a Special Pass for restricted-access attractions, with consumption tracking and refunds.

## Requirements

### Requirement: Pass identity and state
Every pass SHALL have an identifier unique among all passes (SpeedPasses and Special Passes share this space, separate from tickets), an associated individual ticket, a stored price, and a consumed flag that starts as false. Consumed passes SHALL remain in the system as purchase records.

#### Scenario: Pass identifiers unique among passes
- **WHEN** Special Pass `PE01` exists and `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an error is printed and no pass is created

#### Scenario: Pass id may equal a ticket id
- **WHEN** ticket `X1` exists and `SPECIALPASS CREATE;X1;T001` is processed for a valid ticket `T001`
- **THEN** the Special Pass `X1` is created

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of the stored price of its individual ticket, rounded half-up to cents. A Special Pass SHALL cost the same as that stored price. For a ticket inside a group ticket, the stored price already includes the group discount. Later `SYSTEM PRICE` changes SHALL NOT affect pass prices.

#### Scenario: Prices from a 40 EUR ticket
- **WHEN** passes are bought for a ticket stored at 40.00
- **THEN** each SpeedPass costs 8.00 and the Special Pass costs 40.00

#### Scenario: Prices from a discounted group component
- **WHEN** passes are bought for `TG01:12345678Z` stored at 36.00
- **THEN** each SpeedPass costs 7.20 and the Special Pass costs 36.00

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseId>;<ticket>;<quantity>` SHALL create `quantity` (only `1` or `10`) SpeedPasses with identifiers `<purchaseId>:1` … `<purchaseId>:<quantity>`. They belong to an existing individual ticket (standalone or inside a group ticket) dated today or later. It SHALL print `SPEEDPASS CREATED` with the total price, which is the quantity times the unit price, with no package discount. Any invalid input or identifier conflict SHALL create nothing. A ticket MAY have any number of SpeedPasses.

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for `T001` stored at 40.00
- **THEN** passes `SP02:1` to `SP02:10` exist and the printed total is `80.00`

#### Scenario: Single pass
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;SP03;T001;5` is processed
- **THEN** an error is printed and no pass is created

#### Scenario: Conflict creates nothing
- **WHEN** pass `SP04:7` already exists and `SPEEDPASS CREATE;SP04;T001;10` is processed
- **THEN** an error is printed and none of `SP04:1` … `SP04:10` is newly created

#### Scenario: Past ticket or group ticket rejected
- **WHEN** `SPEEDPASS CREATE;SP05;<ticket>;1` names a ticket dated before today, an unknown ticket, or a group ticket identifier
- **THEN** an error is printed and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<id>;<ticket>` SHALL create a Special Pass for an existing individual ticket dated today or later that has never had a Special Pass, consumed or not. It SHALL print `SPECIALPASS CREATED` with its price.

#### Scenario: Buy Special Pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for `T001` stored at 40.00
- **THEN** `PE01` exists for `T001` and the printed price is `40.00`

#### Scenario: Second Special Pass rejected
- **WHEN** `T001` already has a Special Pass (consumed or not) and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is printed and `PE02` is not created

### Requirement: Pass refunds
`SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` SHALL refund (delete and persist) an unconsumed pass of that kind whose ticket is dated strictly after the current system date, and print `SPEEDPASS DELETED` or `SPECIALPASS DELETED`. Consumed passes, passes of tickets dated today or earlier, unknown identifiers, and passes of the other kind SHALL be rejected with an error. Each SpeedPass of a package is refunded separately.

#### Scenario: Refund one pass of a package
- **WHEN** `T002` is dated after today and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** `SP02:3` no longer exists and `SP02:1` still exists

#### Scenario: Consumed pass not refundable
- **WHEN** `SPEEDPASS DELETE;SP02:1` is processed for a consumed pass
- **THEN** an error is printed and the pass still exists

#### Scenario: Today's ticket passes not refundable
- **WHEN** `SPECIALPASS DELETE;PE01` is processed and `PE01`'s ticket is dated today
- **THEN** an error is printed and `PE01` still exists
