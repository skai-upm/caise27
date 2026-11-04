# Proposal

## Why

Passes can be bought and refunded but never used: `ATTRACTION ENTER` ignores them and SPECIAL attractions behave like INDIVIDUAL ones. This change lets visitors use SpeedPasses with an explicit `SPEED` option and makes SPECIAL attractions require and consume the ticket's Special Pass — always atomically, so a failed entry never consumes anything.

## What Changes

- `ATTRACTION ENTER;<atraccion>;<entrada>` keeps normal entry; new `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` requests SpeedPass use (any other third parameter is rejected).
- With `SPEED`, every individual ticket involved (the ticket itself, or each constituent of a group ticket) must have at least one unconsumed SpeedPass; exactly one per individual ticket (the earliest purchased unused one) is consumed, and only if the whole entry succeeds.
- At SPECIAL attractions (individual tickets only) the ticket's Special Pass is located automatically, must be unconsumed, and is consumed only after successful entry; `SPEED` is rejected there.
- If any condition fails (type, date, capacity, already inside, missing/consumed pass), nobody enters and no pass is consumed.
- The success response reports the consumed passes, e.g. `OK: 3 user(s) entered attraction A01; SpeedPasses consumed: P01-1, P02-1, P03-1`.
- Consumption is persisted in `passes.txt`; consumed passes stay recorded and cannot be refunded.

## Capabilities

### New Capabilities
- `pass-access`: Pass-aware attraction entry (SPEED option, Special Pass at SPECIAL attractions, atomicity, consumption reporting).

### Modified Capabilities
- None (normal entry rules in `attraction-access` and `attraction-types` are unchanged and still apply).

## Impact

- **Code**: `PassService` (selection of usable SpeedPasses / Special Pass for a ticket, consumption + save), `ParkController.enterAttraction(attraction, ticket, speed)` returning an entry result with the consumed passes, `CommandProcessor` (optional `SPEED`, response suffix), `CommandCatalog` (syntax `ATTRACTION ENTER;Identificador;Ticket[;SPEED]`), UML.
- **Mandatory milestone message**: `[F3-C08] Extend attraction access and pass consumption`.
- **Assumptions recorded**: the `SPEED` keyword is case-sensitive; when several unused SpeedPasses exist the earliest purchased is consumed; the response of normal entries without consumption is unchanged; `ATTRACTION EXIT` is unchanged (no pass effect).
