# Spec Delta

## Purpose

Defines the personal passes associated with individual tickets — SpeedPasses and Special Passes — including their identity, pricing, consumption state, purchase and refund.

## ADDED Requirements

### Requirement: Pass identity and state
Every pass SHALL have its own identifier, a price, the associated individual ticket and a consumed indicator (initially not consumed). Pass identifiers SHALL be unique among all existing passes of any type (they do not share the ticket identifier space). Consumed passes SHALL remain in the system as purchase records.

#### Scenario: Pass identifiers do not collide
- **GIVEN** a pass `SP01:1` exists
- **WHEN** a purchase would create another pass `SP01:1`
- **THEN** it is rejected with an error message starting with `ERROR`

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of the price stored in its individual ticket and a Special Pass the same as that price, rounded to cents; later `SYSTEM PRICE` changes MUST NOT change them. For composition tickets of a group ticket the discounted ticket price is used.

#### Scenario: Prices from the stored ticket price
- **GIVEN** ticket `T001` bought at 40.00 and then `SYSTEM PRICE;50`
- **WHEN** a SpeedPass and a Special Pass are bought for `T001`
- **THEN** they cost 8.00 and 40.00

#### Scenario: Discounted composition ticket
- **GIVEN** composition ticket `TG02:12345678Z` priced 32.00
- **WHEN** a SpeedPass is bought for it
- **THEN** it costs 6.40

### Requirement: SPEEDPASS CREATE command
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity `1` or `10` SHALL create that many independent SpeedPasses identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>` for an existing individual ticket dated on or after the current date, and print `SPEEDPASS CREATED` with the total price (quantity × unit price, no pack discount). All identifiers SHALL be checked first; on any conflict, an invalid quantity, a missing ticket or a past ticket, nothing is created and an error message starting with `ERROR` is printed.

#### Scenario: Single and pack purchases
- **GIVEN** ticket `T001` at 40.00 dated today or later
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` and `SPEEDPASS CREATE;SP02;T001;10` are processed
- **THEN** `SP01:1` and `SP02:1`…`SP02:10` exist, the totals shown are 8.00 and 80.00

#### Scenario: Invalid quantity, past ticket or conflict
- **WHEN** a purchase with quantity `5`, for a ticket dated before the current date, for a non-existent ticket, or whose generated identifiers include an existing pass identifier is processed
- **THEN** an error message starting with `ERROR` is printed and no pass of that purchase exists

### Requirement: SPECIALPASS CREATE command
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current date and print `SPECIALPASS CREATED` with its price. A ticket SHALL have at most one Special Pass: while one exists for the ticket, consumed or not, another purchase MUST be rejected with an error message starting with `ERROR`.

#### Scenario: Second Special Pass rejected even if consumed
- **GIVEN** ticket `T001` has Special Pass `PE01`, possibly already consumed
- **WHEN** `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error message starting with `ERROR` is printed and `PE02` does not exist

### Requirement: Pass refunds
`SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` SHALL refund (remove) a pass of the corresponding type and print `SPEEDPASS DELETED` / `SPECIALPASS DELETED` only when it is not consumed and its ticket's date is strictly after the current date; each pass of a pack is refunded separately. Otherwise, or for an unknown identifier, an error message starting with `ERROR` is printed and the pass is kept.

#### Scenario: Refund one pass of a pack
- **GIVEN** future ticket `T001` with SpeedPasses `SP02:1`…`SP02:10`
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** `SPEEDPASS DELETED` is printed and the other nine passes remain

#### Scenario: Consumed or not-future pass not refunded
- **WHEN** a refund is requested for a consumed pass, or for a pass whose ticket is dated today or earlier
- **THEN** an error message starting with `ERROR` is printed and the pass remains
