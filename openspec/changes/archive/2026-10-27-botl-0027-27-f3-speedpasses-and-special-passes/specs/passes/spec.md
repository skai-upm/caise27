# Spec Delta

## Purpose

Defines SpeedPasses and Special Passes: personal purchases attached to individual tickets, with their pricing, consumption state, refunds and removal together with their ticket.

## ADDED Requirements

### Requirement: Pass identity and state
Every pass SHALL have its own identifier (unique among all existing passes), a price, the individual ticket it is associated with and a consumed indicator; consumed passes SHALL remain in the system.

#### Scenario: Pass created unconsumed
- **WHEN** a SpeedPass or Special Pass is bought
- **THEN** it references its individual ticket and is not consumed

#### Scenario: Duplicate pass identifier
- **WHEN** pass `PE01` exists and `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an error is reported and no pass is created

### Requirement: Pass prices
A SpeedPass SHALL cost 20% and a Special Pass 100% of the price stored in its individual ticket, including any group discount; changes of the system price SHALL NOT affect them.

#### Scenario: Prices from a full-price ticket
- **WHEN** `T001` cost 40.00
- **THEN** a SpeedPass for it costs 8.00 and a Special Pass 40.00

#### Scenario: Prices from a discounted group component
- **WHEN** `TG01:12345678Z` cost 36.00 (10% group discount) and the system price is later changed to 50
- **THEN** a SpeedPass for it costs 7.20

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL accept quantity 1 or 10 only, require an existing individual ticket dated on or after the current date, create independent passes identified `<purchaseIdentifier>:1` … `:<quantity>` only if none of those identifiers exists, and display the total price (quantity × unit price, no package discount).

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket
- **THEN** passes `SP02:1` to `SP02:10` exist and the response is `SPEEDPASS CREATED - Total price: 80.00 EUR`

#### Scenario: Single pass naming
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists and the response shows 8.00

#### Scenario: Invalid quantity or ticket
- **WHEN** the quantity is 5, the ticket does not exist, or the ticket's date is before the current date
- **THEN** an error is reported and no pass is created

#### Scenario: Identifier conflict prevents partial creation
- **WHEN** pass `SP03:7` exists and `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** an error is reported and none of `SP03:1`…`SP03:10` besides the existing one exists

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current date that does not already have one (consumed or not), displaying its price.

#### Scenario: Buy a Special Pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for a 40.00 ticket
- **THEN** the response is `SPECIALPASS CREATED - Price: 40.00 EUR`

#### Scenario: Second Special Pass rejected
- **WHEN** `T001` already has a Special Pass and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is reported

### Requirement: Pass refunds
`SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` SHALL refund (delete) a pass of that type only when it is not consumed and its ticket is dated strictly after the current date; each pass of a package is refunded individually.

#### Scenario: Refund one pass of a package
- **WHEN** the current date is before `T001`'s date and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** the response is `SPEEDPASS DELETED`, `SP02:3` no longer exists and `SP02:1` still does

#### Scenario: Ticket for today or consumed pass
- **WHEN** the pass's ticket is dated today or earlier, or the pass is consumed
- **THEN** the refund is rejected and the pass remains

### Requirement: Passes removed with their ticket
When TICKET DELETE or GROUPTICKET DELETE validly deletes tickets, all passes associated with the deleted individual tickets SHALL be deleted too, consistently.

#### Scenario: Ticket deletion removes its passes
- **WHEN** future ticket `T001` has passes from several purchases and `TICKET DELETE;T001` succeeds
- **THEN** none of its passes exists

#### Scenario: Group ticket deletion removes component passes
- **WHEN** future group ticket `TG01` has a Special Pass on `TG01:12345678Z` and `GROUPTICKET DELETE;TG01` succeeds
- **THEN** the pass, the component tickets and `TG01` no longer exist
