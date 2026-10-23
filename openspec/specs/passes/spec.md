# passes Specification

## Purpose

Defines the personal passes bought for individual tickets — SpeedPasses for faster entry and Special Passes for restricted attractions — including their pricing, identifiers, consumption state and refunds.

## Requirements

### Requirement: Pass identity and state
Every pass SHALL belong to exactly one individual ticket (standalone or a member ticket of a group ticket) and keep its identifier, stored price and a consumed flag. Pass identifiers SHALL be unique among all passes (ignoring case); they need not differ from ticket identifiers. Consumed passes SHALL remain recorded while their ticket exists.

#### Scenario: Duplicate pass identifier
- **WHEN** pass `PE01` exists and `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an error is printed and no pass is created

### Requirement: Pass pricing
A SpeedPass SHALL cost 20% of the price stored in its individual ticket and a Special Pass SHALL cost exactly that price, both rounded to cents. For member tickets of a group ticket, the discounted member price SHALL be used. Changes to the system price SHALL NOT affect existing tickets or the passes bought for them.

#### Scenario: Prices from a 40.00 ticket
- **WHEN** T001 costs 40.00 EUR and the system price is later set to 50.00
- **THEN** a SpeedPass for T001 costs 8.00 EUR and a Special Pass for T001 costs 40.00 EUR

#### Scenario: Discounted member ticket
- **WHEN** member ticket `TG01:12345678Z` costs 36.00 EUR
- **THEN** a SpeedPass for it costs 7.20 EUR

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseId>;<ticket>;<quantity>` SHALL, for an existing individual ticket dated on or after the system date and a quantity of exactly `1` or `10`, create that many independent SpeedPasses identified `<purchaseId>:1` … `<purchaseId>:<quantity>` and print the total charged (quantity × unit price, no package discount). All identifiers SHALL be checked first; on any conflict or invalid input nothing is created. A ticket MAY have any number of SpeedPasses.

#### Scenario: Package of ten
- **WHEN** T001 costs 40.00 EUR and `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` to `SP02:10` exist and a total of 80.00 EUR is printed

#### Scenario: Single pass
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists and its price is printed

#### Scenario: Invalid quantity or ticket
- **WHEN** the quantity is `5`, the ticket does not exist, is a group ticket, or is dated before the system date
- **THEN** an error is printed and no pass is created

#### Scenario: Partial conflict
- **WHEN** pass `SP03:7` already exists and `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** an error is printed and none of `SP03:1` … `SP03:10` is created by this command

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<id>;<ticket>` SHALL create a Special Pass for an existing individual ticket dated on or after the system date that has no Special Pass, and print its price. A consumed Special Pass still counts, so a second one SHALL be rejected; a refunded one no longer exists and no longer counts.

#### Scenario: Buy Special Pass
- **WHEN** T001 costs 40.00 EUR and has no Special Pass and `SPECIALPASS CREATE;PE01;T001` is processed
- **THEN** PE01 exists for T001 and a price of 40.00 EUR is printed

#### Scenario: Second Special Pass
- **WHEN** T001 already has a Special Pass (consumed or not) and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is printed and PE02 is not created

### Requirement: Pass refunds
`SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` SHALL delete (refund) a single pass of the matching type only if it is not consumed and its ticket is dated strictly after the system date. Each SpeedPass of a package is refunded on its own identifier.

#### Scenario: Refund future pass
- **WHEN** SP05:3 is unconsumed and its ticket is dated after the system date
- **THEN** `SPEEDPASS DELETE;SP05:3` prints `SPEEDPASS DELETED` and SP05:3 no longer exists while the other SP05 passes remain

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, or its ticket is dated today or earlier, or the identifier names a pass of the other type
- **THEN** an error is printed and the pass is unchanged
