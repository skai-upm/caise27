# passes Specification

## Purpose
Defines SpeedPasses and Special Passes: personal extras bought for a specific individual ticket, with their own identity, price derived from the ticket's stored price, consumption status and refund rules.

## Requirements

### Requirement: Pass identity and data
Every pass SHALL have a unique identifier among passes, its price, the individual ticket it is associated with, and whether it has been consumed. Passes MAY be associated with individual tickets that compose a group ticket. A consumed pass SHALL remain in the system as a purchase record.

#### Scenario: Pass for a composed ticket
- **GIVEN** individual ticket `TG01:12345678Z` is part of group ticket `TG01`
- **WHEN** `SPEEDPASS CREATE;SP05;TG01:12345678Z;1` is processed
- **THEN** pass `SP05:1` is created for `TG01:12345678Z`

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of the stored price of its individual ticket, and a Special Pass SHALL cost the stored price of its individual ticket. Changes made with SYSTEM PRICE after the ticket was bought SHALL NOT affect pass prices. For tickets in a group ticket, the discounted ticket price SHALL be used.

#### Scenario: Prices from the ticket
- **GIVEN** ticket `T001` was bought at 40.00 and `SYSTEM PRICE;50.00` was processed afterwards
- **WHEN** a SpeedPass and a Special Pass are bought for `T001`
- **THEN** the SpeedPass costs 8.00 and the Special Pass costs 40.00

#### Scenario: Discounted composed ticket
- **GIVEN** composed ticket `TG02:DNI1` costs 32.00 because of a group discount
- **WHEN** a SpeedPass is bought for it
- **THEN** it costs 6.40

### Requirement: Buy SpeedPasses
The system SHALL, for `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity `1` or `10`, create that many independent SpeedPasses identified `<purchaseIdentifier>:1` to `<purchaseIdentifier>:<quantity>` for the individual ticket. The ticket must exist and be dated on or after the current system date. All identifiers SHALL be checked first, and if any conflicts, no pass is created. The total charged (quantity × unit price, no package discount) SHALL be displayed.

#### Scenario: Package of ten
- **GIVEN** ticket `T001` at 40.00 dated today
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` to `SP02:10` exist and the displayed total is 80.00

#### Scenario: Single pass
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists and the displayed total is 8.00

#### Scenario: Rejected purchases
- **WHEN** the quantity is not 1 or 10, the ticket does not exist or is dated before today, or one of the generated identifiers already exists
- **THEN** an error message is displayed and no pass of that purchase is created

### Requirement: Buy a Special Pass
The system SHALL, for `SPECIALPASS CREATE;<identifier>;<individualTicket>`, create a Special Pass for the individual ticket when the identifier is unused among passes, the ticket exists and is dated on or after the current system date, and the ticket has no Special Pass in the system, whether consumed or not. The price charged SHALL be displayed.

#### Scenario: Buy a Special Pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for a valid ticket at 40.00
- **THEN** `PE01` exists for `T001` and the displayed price is 40.00

#### Scenario: Second Special Pass rejected even after consumption
- **GIVEN** ticket `T001` has Special Pass `PE01`, which has been consumed
- **WHEN** `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error message is displayed and `PE02` is not created

### Requirement: Refund passes
The system SHALL refund (delete) a pass with `SPEEDPASS DELETE;<speedPassIdentifier>` or `SPECIALPASS DELETE;<specialPassIdentifier>` only if the pass exists, is of that kind, has not been consumed, and its individual ticket is dated strictly after the current system date. Each SpeedPass of a package SHALL be refundable separately. A refunded pass SHALL no longer be part of the stored state.

#### Scenario: Refund one pass of a package
- **GIVEN** passes `SP02:1` to `SP02:10` for a ticket dated tomorrow
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** a confirmation is displayed, `SP02:3` no longer exists, and the other nine remain

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, its ticket is dated today or earlier, the pass does not exist, or the command names the wrong pass kind
- **THEN** an error message is displayed and the pass remains (if it exists)

### Requirement: PASS commands listed in HELP
The system SHALL include `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>` and `SPECIALPASS DELETE;<specialPassIdentifier>` in `HELP` and, with their purpose, in `HELP EXTENDED`.

#### Scenario: HELP shows pass commands
- **WHEN** `HELP` is processed
- **THEN** the four pass command syntaxes are shown
