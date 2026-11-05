# Spec Delta

## Purpose

Defines the SpeedPasses and Special Passes that can be purchased for individual tickets (including tickets in a group ticket composition): their identity, prices derived from the ticket's stored price, purchase rules and commands, consumption state, refunds, and their removal together with the ticket they belong to.

## ADDED Requirements

### Requirement: Passes are personal purchases tied to an individual ticket
The system SHALL model SpeedPasses and Special Passes as passes with their own identity that retain at least their identifier, price, associated individual ticket and a consumed indicator. Passes SHALL be associable with any existing individual ticket, including the individual tickets of a group ticket composition. Pass identifiers SHALL be unique among all existing passes (SpeedPasses and Special Passes share one identifier space, independent of ticket identifiers). A consumed pass SHALL remain stored as a record of the purchase while its ticket exists.

#### Scenario: Pass bought for a group ticket component
- **GIVEN** group ticket `TG01` dated after the system date includes component ticket `TG01:12345678Z`
- **WHEN** `SPECIALPASS CREATE;PE01;TG01:12345678Z` is processed
- **THEN** the response starts with `OK` and Special Pass `PE01` is associated with `TG01:12345678Z`

#### Scenario: Pass identifiers do not collide with ticket identifiers
- **GIVEN** a ticket with identifier `T001` exists
- **WHEN** `SPECIALPASS CREATE;T001;T001` is processed
- **THEN** the response starts with `OK`

### Requirement: Pass prices derive from the ticket's stored price
A SpeedPass SHALL cost 20% of the price stored in its individual ticket, and a Special Pass SHALL cost the price stored in its individual ticket, both rounded to 2 decimals. Later changes made with `SYSTEM PRICE` SHALL NOT change the price of existing tickets nor the price of passes bought for them; passes for group ticket components SHALL use the component's discounted price.

#### Scenario: Prices from a ticket bought at 40.00
- **GIVEN** ticket `T001` was bought at 40.00 EUR and then `SYSTEM PRICE;60` was executed
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` and `SPECIALPASS CREATE;PE01;T001` are processed
- **THEN** `SP01:1` costs `8.00` EUR and `PE01` costs `40.00` EUR

#### Scenario: Prices from a discounted component ticket
- **GIVEN** component ticket `TG01:12345678Z` was bought at 36.00 EUR (10% group discount)
- **WHEN** `SPEEDPASS CREATE;SP09;TG01:12345678Z;1` is processed
- **THEN** `SP09:1` costs `7.20` EUR

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create `quantity` independent SpeedPasses, identified `<purchaseIdentifier>:1` to `<purchaseIdentifier>:<quantity>`, for the individual ticket, and print a response starting with `OK` that shows the total price charged (quantity times the unit price, with no package discount). The quantity SHALL be exactly `1` or `10`. The purchase SHALL be completed only if the individual ticket exists and its date is equal to or later than the current system date, and only if none of the generated identifiers is already used by an existing pass; otherwise it SHALL be rejected with an `ERROR` response and no pass SHALL be created. An individual ticket MAY have any number of SpeedPasses.

#### Scenario: Single SpeedPass
- **GIVEN** ticket `T001` at 40.00 EUR dated today or later
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** the response starts with `OK`, reports a total of `8.00` EUR, and exactly SpeedPass `SP01:1` exists

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** SpeedPasses `SP02:1` to `SP02:10` exist, each costing `8.00` EUR, and the response reports a total of `80.00` EUR

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;SP03;T001;5` is processed
- **THEN** an `ERROR` response is printed and no `SP03:<n>` pass exists

#### Scenario: Identifier conflict prevents the whole package
- **GIVEN** SpeedPass `SP02:7` already exists
- **WHEN** `SPEEDPASS CREATE;SP02;T002;10` is processed
- **THEN** an `ERROR` response is printed and no new pass is created

#### Scenario: Unknown, group or past ticket
- **WHEN** a SpeedPass is requested for a ticket that does not exist, for a group ticket identifier, or for an individual ticket dated before the current system date
- **THEN** an `ERROR` response is printed and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create one Special Pass for the individual ticket and print a response starting with `OK` that shows the price charged. The individual ticket SHALL exist and have a date equal to or later than the current system date, the identifier SHALL not be used by an existing pass, and the ticket SHALL NOT already have a Special Pass, whether consumed or not; otherwise the command SHALL be rejected with an `ERROR` response. A refunded Special Pass no longer exists, so after a refund the ticket MAY purchase a Special Pass again.

#### Scenario: Special Pass purchased
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for ticket `T001` at 40.00 EUR dated today or later
- **THEN** the response starts with `OK` and reports the price `40.00` EUR

#### Scenario: Second Special Pass rejected even after consumption
- **GIVEN** ticket `T001` has Special Pass `PE01`, consumed or not
- **WHEN** `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an `ERROR` response is printed and `PE02` does not exist

#### Scenario: Duplicate pass identifier
- **GIVEN** Special Pass `PE01` exists for ticket `T001`
- **WHEN** `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an `ERROR` response is printed

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL refund (delete) an existing pass of the corresponding type and print a response starting with `OK` with the refunded amount, only when the pass has not been consumed and the date of its individual ticket is strictly later than the current system date. Each SpeedPass of a package SHALL be refundable separately by its own identifier. Otherwise the command SHALL be rejected with an `ERROR` response and the pass SHALL be kept. A refunded pass ceases to be part of the system state.

#### Scenario: Unconsumed future pass refunded
- **GIVEN** the system date is 2027-04-01 and `SP02:3` belongs to ticket `T001` dated 2027-04-15
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** the response starts with `OK`, `SP02:3` no longer exists, and `SP02:1`, `SP02:2` and `SP02:4` to `SP02:10` still exist

#### Scenario: Consumed pass not refunded
- **GIVEN** Special Pass `PE01` has been consumed
- **WHEN** `SPECIALPASS DELETE;PE01` is processed
- **THEN** an `ERROR` response is printed and `PE01` still exists

#### Scenario: Pass of a ticket for today or earlier not refunded
- **GIVEN** `SP01:1` belongs to a ticket dated on the current system date
- **WHEN** `SPEEDPASS DELETE;SP01:1` is processed
- **THEN** an `ERROR` response is printed and `SP01:1` still exists

#### Scenario: Wrong type or unknown identifier
- **WHEN** `SPECIALPASS DELETE;SP01:1` or `SPEEDPASS DELETE;NOPE` is processed
- **THEN** an `ERROR` response is printed
