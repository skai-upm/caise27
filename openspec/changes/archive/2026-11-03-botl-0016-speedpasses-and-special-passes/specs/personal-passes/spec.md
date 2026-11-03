# Spec Delta

## Purpose

Lets visitors buy personal add-ons for an individual ticket: SpeedPasses, singly or in packs of ten, and one Special Pass per ticket, each with its own price and consumption status, refundable while unused and in time.

## ADDED Requirements

### Requirement: Pass data
Every pass SHALL record its identifier, its price, the individual ticket it is associated with (an individual ticket or a constituent ticket of a group ticket) and whether it has been consumed. Pass identifiers SHALL be unique among all passes.

#### Scenario: Pass fields
- **GIVEN** ticket `T001` with stored price 40.00
- **WHEN** `SPECIALPASS CREATE;SP1;T001` succeeds
- **THEN** pass `SP1` records price 40.00, ticket `T001` and consumed = false

### Requirement: SpeedPass purchase
The command `SPEEDPASS CREATE;<IdCompra>;<IdTicket>;<Cantidad>` SHALL accept only the quantities 1 and 10 and SHALL create that many independent SpeedPasses with identifiers `<IdCompra>-1` to `<IdCompra>-<Cantidad>`, each priced at 20% of the associated ticket's stored price, responding `OK SPEEDPASS CREATED -> <total>` with the total of the purchase. The ticket SHALL exist and be dated on or after the system date. If any derived identifier already exists, nothing SHALL be created.

#### Scenario: Single SpeedPass
- **GIVEN** ticket `T001` priced 40.00
- **WHEN** `SPEEDPASS CREATE;S1;T001;1` is processed
- **THEN** the response is `OK SPEEDPASS CREATED -> 8.00` and pass `S1-1` exists

#### Scenario: Pack of ten
- **WHEN** `SPEEDPASS CREATE;S2;T001;10` is processed
- **THEN** the response is `OK SPEEDPASS CREATED -> 80.00` and ten passes `S2-1` to `S2-10`, each priced 8.00, exist

#### Scenario: Disallowed quantity
- **WHEN** `SPEEDPASS CREATE;S3;T001;2`, `...;0` or `...;abc` is processed
- **THEN** an error is reported and nothing is created

#### Scenario: Identifier conflict
- **GIVEN** pass `S4-5` already exists
- **WHEN** `SPEEDPASS CREATE;S4;T001;10` is processed
- **THEN** an error is reported and none of `S4-1` to `S4-10` other than the existing one exists

#### Scenario: Unknown or past ticket
- **WHEN** the ticket does not exist or is dated before the system date
- **THEN** an error is reported and nothing is created

### Requirement: Special Pass purchase
The command `SPECIALPASS CREATE;<IdPase>;<IdTicket>` SHALL create a Special Pass priced at 100% of the associated ticket's stored price, responding `OK SPECIALPASS CREATED -> <precio>`. A ticket SHALL have at most one Special Pass: a purchase for a ticket that already has one, consumed or not, SHALL be rejected. Identifier and ticket rules are those of SpeedPasses.

#### Scenario: Special Pass
- **WHEN** `SPECIALPASS CREATE;SP1;T001` is processed for `T001` priced 40.00
- **THEN** the response is `OK SPECIALPASS CREATED -> 40.00`

#### Scenario: Second Special Pass
- **GIVEN** `T001` already has Special Pass `SP1`
- **WHEN** `SPECIALPASS CREATE;SP2;T001` is processed
- **THEN** an error is reported

### Requirement: Price from the stored ticket price
Pass prices SHALL be computed from the price stored in the associated individual ticket at purchase time and SHALL then be kept unchanged in the pass.

#### Scenario: Discounted constituent ticket
- **GIVEN** group ticket `TG01` of 3 members at individual price 40.00, so constituent ticket `TG01-1` stores 36.00
- **WHEN** `SPEEDPASS CREATE;S5;TG01-1;1` and `SPECIALPASS CREATE;SP5;TG01-1` are processed
- **THEN** the responses are `OK SPEEDPASS CREATED -> 7.20` and `OK SPECIALPASS CREATED -> 36.00`

#### Scenario: Later price changes
- **GIVEN** `S1-1` bought at 8.00
- **WHEN** `SYSTEM PRICE;60.00` is processed
- **THEN** `S1-1` still costs 8.00

### Requirement: Pass refund
`SPEEDPASS DELETE;<IdPase>` and `SPECIALPASS DELETE;<IdPase>` SHALL refund (remove) a pass of the corresponding type only if it has not been consumed and its ticket is dated strictly after the system date; otherwise, or for an unknown pass or a pass of the other type, they SHALL be rejected. Consumed passes SHALL remain recorded.

#### Scenario: Refund of a future pass
- **GIVEN** system date 2026-10-01 and `S1-1` on `T001` dated 2026-10-15
- **WHEN** `SPEEDPASS DELETE;S1-1` is processed
- **THEN** the pass is removed and its price is reported as refunded

#### Scenario: Not refundable
- **WHEN** the pass is consumed, its ticket is dated on or before the system date, or the command names a pass of the other type
- **THEN** an error is reported and the pass is kept
