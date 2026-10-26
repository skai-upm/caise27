# Proposal

## Why

SpeedPasses and Special Passes can be bought but not yet used. Delivery 3 extends attraction entry so visitors can use them, respecting each attraction type, group atomicity and the rule that passes are only consumed by a successful entry.

## What Changes

- `ATTRACTION ENTER;<attraction>;<ticket>` keeps normal entry; `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` requests SpeedPass use.
- With `SPEED`, an individual ticket consumes exactly one unused SpeedPass of that ticket; a group ticket consumes one unused SpeedPass of each individual ticket composing it. If any member lacks one, or any other entry condition fails, nobody enters and nothing is consumed.
- `SPECIAL` attractions: entry (individual tickets only) automatically uses the ticket's Special Pass, which must exist and be unused; it is marked consumed only after a successful entry. `SPEED` is rejected there.
- The response reports which SpeedPasses or which Special Pass were consumed; consumption is persisted.
- Admission by type from C07 still applies (GENERAL/INDIVIDUAL: individual with optional SPEED; GENERAL/GROUP: group with optional SPEED).

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attractions`: entry syntax with `SPEED`, SpeedPass consumption, Special Pass rules at SPECIAL attractions, response reporting consumption.

## Impact

- `Ticket.getIndividualTickets()`; `Attraction` pass hooks (`acceptsSpeed`, `requiresSpecialPass`) overridden by `SpecialAttraction`; `AttractionController.enter` with pass handling (now depends on `PassController`); `PassController` lookup/consume methods; `CommandProcessor`, catalog, `App` order of configuration, tests, `uml/model.puml`.
