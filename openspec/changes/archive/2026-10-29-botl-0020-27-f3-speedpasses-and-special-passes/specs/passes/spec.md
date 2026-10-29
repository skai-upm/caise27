# Spec Delta

## Purpose

Defines SpeedPasses and Special Passes: personal passes associated with individual tickets, their purchase and pricing, consumption state, refunds, removal together with their ticket, and persistence.

## ADDED Requirements

### Requirement: Pass information
Every pass SHALL have its own identifier, a price, its associated individual ticket and a consumed indicator; pass identifiers SHALL be unique among existing passes (independently of ticket identifiers); consumed passes SHALL remain in the system as purchase records. Passes MAY be associated with individual tickets that belong to a group ticket.

#### Scenario: Pass on a group-ticket component
- **WHEN** `TG01:12345678Z` belongs to group ticket `TG01` dated today and `SPECIALPASS CREATE;PE01;TG01:12345678Z` is processed
- **THEN** the Special Pass is created for that individual ticket

### Requirement: Pass prices
A SpeedPass SHALL cost 20 % of its individual ticket's stored price and a Special Pass SHALL cost the ticket's stored price (discounted price for group-ticket components); later SYSTEM PRICE changes SHALL NOT affect them.

#### Scenario: Prices from the ticket price
- **WHEN** `T001` was bought at 40.00, `SYSTEM PRICE;50` is processed and passes are bought for `T001`
- **THEN** each SpeedPass costs 8.00 and the Special Pass costs 40.00

#### Scenario: Discounted component
- **WHEN** an individual ticket of a 6-member group ticket costs 32.00
- **THEN** a SpeedPass for it costs 6.40

### Requirement: Buy SpeedPasses
The command `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create `<quantity>` independent SpeedPasses identified `<purchaseIdentifier>:1` to `<purchaseIdentifier>:<quantity>` and display the total charged (quantity × unit price, no extra discount). The quantity SHALL be 1 or 10; the individual ticket SHALL exist with a date equal to or later than the current date; all identifiers SHALL be free, otherwise nothing SHALL be created and an error message SHALL be shown.

#### Scenario: Package of ten
- **WHEN** `T001` (40.00, dated today) exists and `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` to `SP02:10` exist and the total 80.00 is displayed

#### Scenario: Single pass naming
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` exists

#### Scenario: Rejections create nothing
- **WHEN** the quantity is 5, the ticket is unknown or in the past, or `SP02:3` already exists when buying `SP02` ×10
- **THEN** an error message is shown and no pass of that purchase is created

### Requirement: Buy a Special Pass
The command `SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated today or later and display its price. An individual ticket SHALL have at most one Special Pass; while one exists for the ticket, consumed or not, another purchase SHALL be rejected, as SHALL a duplicate pass identifier.

#### Scenario: Second Special Pass rejected
- **WHEN** `PE01` exists for `T001` and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error message is shown and `PE02` is not created

### Requirement: Refund passes
The commands `SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL refund (remove from the system) a pass of the corresponding type only if it has not been consumed and its ticket's date is strictly later than the current date; each SpeedPass of a package SHALL be refundable separately. Other cases SHALL be rejected with an error message.

#### Scenario: Refund one pass of a package
- **WHEN** `SP02:1`..`SP02:10` exist for a future ticket and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** `SP02:3` no longer exists and the other nine remain

#### Scenario: Refund not allowed
- **WHEN** the pass is consumed, its ticket is for today or earlier, the identifier is unknown, or it is the wrong pass type
- **THEN** an error message is shown and the pass remains

### Requirement: Passes removed with their ticket
When a future individual ticket is validly deleted with TICKET DELETE, all its SpeedPasses and its Special Pass SHALL be removed together; when a future group ticket is validly deleted with GROUPTICKET DELETE, the passes of all its composed individual tickets, the composed tickets and the group ticket SHALL be removed, without partial deletions.

#### Scenario: Ticket deletion removes passes
- **WHEN** future ticket `T001` has passes from several purchases and `TICKET DELETE;T001` is processed
- **THEN** `T001` and all its passes no longer exist

#### Scenario: Group ticket deletion removes component passes
- **WHEN** a component of future group ticket `TG01` has a SpeedPass and `GROUPTICKET DELETE;TG01` is processed
- **THEN** the group ticket, its components and that SpeedPass no longer exist

### Requirement: Passes persisted
Passes SHALL be stored with their type, identifier, associated ticket, price and consumed state and reconstructed on restart, using the same repository mechanism in normal and `--fly` mode.

#### Scenario: Restart keeps passes
- **WHEN** passes exist and the application restarts with the same data directory
- **THEN** the passes exist with the same ticket, price and consumed state
