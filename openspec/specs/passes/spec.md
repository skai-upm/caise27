# passes Specification

## Purpose

Defines the personal extras that can be bought for an Amusement Park individual ticket — SpeedPasses and the Special Pass — their prices, consumption state and refunds, and what happens to them when their ticket is refunded.

## Requirements

### Requirement: Common pass information
Every pass SHALL have its own identifier, its price, the individual ticket it belongs to (bought directly or as part of a group ticket) and a consumed indicator, through a common pass abstraction. Pass identifiers MUST be unique among existing passes of both types; they do not share the ticket identifier space. A consumed pass MUST remain in the system as a purchase record.

#### Scenario: Pass on a group-ticket constituent
- **GIVEN** constituent ticket `TG01:12345678Z` of an active group ticket
- **WHEN** `SPECIALPASS CREATE;PE01;TG01:12345678Z` is processed
- **THEN** pass `PE01` is created for that individual ticket

#### Scenario: Pass identifiers independent from tickets
- **GIVEN** a ticket with identifier `X1`
- **WHEN** `SPECIALPASS CREATE;X1;T001` is processed
- **THEN** the pass is created

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of the price stored in its individual ticket and a Special Pass the same as that price, rounded to cents; the current system price MUST NOT affect them, and the discounted price of a group-ticket constituent MUST be used.

#### Scenario: Prices from the ticket
- **GIVEN** ticket `T001` bought at `40.00` and the system price later set to `50`
- **WHEN** a SpeedPass and a Special Pass are bought for `T001`
- **THEN** they cost `8.00` and `40.00`

#### Scenario: Discounted constituent
- **GIVEN** constituent `TG02:12345678Z` priced `32.00` (20% group discount)
- **WHEN** a SpeedPass is bought for it
- **THEN** it costs `6.40`

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchase>;<ticket>;<quantity>` SHALL create `quantity` independent SpeedPasses identified `<purchase>:1` … `<purchase>:<quantity>` for an existing individual ticket dated on or after the current system date, and print the total price charged (quantity × unit price, no package discount). Quantity MUST be `1` or `10`. All identifiers MUST be checked first: on any conflict nothing is created. An unlimited number of SpeedPasses may belong to one ticket.

#### Scenario: Package of ten
- **GIVEN** ticket `T001` priced `40.00` and dated today
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` to `SP02:10` exist, each priced `8.00`, and the printed total is `80.00`

#### Scenario: Single pass naming
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` is created

#### Scenario: Invalid quantity, past ticket or conflict
- **WHEN** a SpeedPass purchase uses quantity `5`, or a ticket dated before today, or a purchase identifier whose generated identifier already exists
- **THEN** an error is printed and no pass is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<id>;<ticket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current system date and print its price. An individual ticket MUST have at most one existing Special Pass; a second purchase is rejected even if the first was consumed.

#### Scenario: Second Special Pass rejected
- **GIVEN** ticket `T001` already has Special Pass `PE01` (consumed or not)
- **WHEN** `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error reporting that the ticket already has a Special Pass is printed

### Requirement: Pass refunds
`SPEEDPASS DELETE;<pass>` and `SPECIALPASS DELETE;<id>` SHALL refund (remove) an unconsumed pass of the named type whose individual ticket is dated strictly after the current system date. Consumed passes, passes of tickets for today or earlier, unknown identifiers and passes of the other type MUST be rejected with an error and kept. SpeedPasses bought in a package are refunded individually.

#### Scenario: One pass of a package refunded
- **GIVEN** future ticket `T001` with passes `SP02:1`..`SP02:10`
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** only `SP02:3` is removed

#### Scenario: Refund rejected for consumed pass or current ticket
- **WHEN** a refund is requested for a consumed pass, or for a pass whose ticket is dated today
- **THEN** an error is printed and the pass is kept

### Requirement: Ticket refunds remove their passes
When a future individual ticket is validly deleted with `TICKET DELETE`, all its SpeedPasses and its Special Pass SHALL be removed too, whatever purchase created them. When a future group ticket is validly deleted with `GROUPTICKET DELETE`, the passes of all its constituent tickets, the constituents and the group ticket SHALL be removed. If the deletion is rejected, nothing is removed.

#### Scenario: Individual ticket refund
- **GIVEN** future ticket `T001` with passes `SP01:1` and `PE01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `T001`, `SP01:1` and `PE01` no longer exist

#### Scenario: Rejected deletion keeps passes
- **GIVEN** ticket `T001` dated today with pass `SP01:1`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is printed and `SP01:1` still exists
