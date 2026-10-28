# Spec Delta

## Purpose

Defines the personal passes associated with individual tickets — SpeedPasses and Special Passes — including their identity, pricing, purchase rules, consumption state, refunds and deletion together with their ticket.

## ADDED Requirements

### Requirement: Pass data
Every pass SHALL have its own identifier, a price, the individual ticket it is associated with and a consumed indicator. Pass identifiers SHALL be unique among all existing passes of both types. Consumed passes SHALL remain in the system as purchase records.

#### Scenario: New pass state
- **WHEN** a pass is bought
- **THEN** it references its individual ticket and is not consumed

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of the price stored in its individual ticket and a Special Pass SHALL cost 100% of it; for tickets in a group ticket composition, the discounted ticket price is used. Later changes of the base price SHALL NOT affect them.

#### Scenario: Prices from a 40.00 ticket
- **WHEN** `T001` cost 40.00 and a SpeedPass and a Special Pass are bought for it after `SYSTEM PRICE;50.00`
- **THEN** the SpeedPass costs 8.00 and the Special Pass costs 40.00

#### Scenario: Group composition ticket
- **WHEN** a composition ticket of a 3-member group ticket cost 36.00
- **THEN** a SpeedPass for it costs 7.20

### Requirement: SPEEDPASS CREATE command
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity `1` or `10` SHALL create that many independent SpeedPasses identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>` and print `SPEEDPASS CREATED. Total price: <total> EUR` (quantity × SpeedPass price, no pack discount). The individual ticket SHALL exist and be dated on or after the current system date; all identifiers SHALL be checked first and on any conflict nothing SHALL be created. Other quantities SHALL be rejected.

#### Scenario: Pack of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket dated today
- **THEN** passes `SP02:1` … `SP02:10` exist and `SPEEDPASS CREATED. Total price: 80.00 EUR` is printed

#### Scenario: Identifier conflict
- **WHEN** `SP02:3` already exists and `SPEEDPASS CREATE;SP02;T002;10` is processed
- **THEN** an `ERROR:` line is printed and no new pass is created

#### Scenario: Invalid purchase
- **WHEN** the quantity is `5`, or the ticket does not exist, or the ticket is dated before the current date
- **THEN** an `ERROR:` line is printed and nothing is created

### Requirement: SPECIALPASS CREATE command
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for the existing individual ticket dated on or after the current date and print `SPECIALPASS CREATED. Price: <price> EUR`. A ticket SHALL have at most one Special Pass: if one exists for it, consumed or not, the purchase SHALL be rejected.

#### Scenario: Second Special Pass
- **WHEN** `T001` already has Special Pass `PE01` and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an `ERROR:` line is printed

### Requirement: Pass refunds
`SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` SHALL delete (refund) the pass of that type and print `SPEEDPASS DELETED` / `SPECIALPASS DELETED` only if it is not consumed and its ticket is dated strictly after the current system date; otherwise, or for an unknown identifier, an `ERROR:` message SHALL be printed. Each pass of a pack is refunded separately.

#### Scenario: Refund one pass of a pack
- **WHEN** `SP02:1`…`SP02:10` belong to a ticket dated tomorrow and `SPEEDPASS DELETE;SP02:4` is processed
- **THEN** `SPEEDPASS DELETED` is printed and the other nine passes remain

#### Scenario: Ticket for today
- **WHEN** the pass's ticket is dated today
- **THEN** the refund is rejected with an `ERROR:` line

#### Scenario: Consumed pass
- **WHEN** the pass is consumed
- **THEN** the refund is rejected with an `ERROR:` line

### Requirement: Passes deleted with their ticket
When `TICKET DELETE` validly deletes an individual ticket, all its SpeedPasses and its Special Pass SHALL be deleted too; when `GROUPTICKET DELETE` validly deletes a group ticket, all passes of its composition tickets, the composition tickets and the group ticket SHALL be deleted. If the ticket deletion is rejected, no pass SHALL be deleted.

#### Scenario: Cascade on ticket deletion
- **WHEN** future ticket `T002` has passes from two different purchases and `TICKET DELETE;T002` succeeds
- **THEN** none of those passes exists any more

#### Scenario: Rejected deletion keeps passes
- **WHEN** `TICKET DELETE;T001` is rejected because `T001` is dated today
- **THEN** all passes of `T001` still exist
