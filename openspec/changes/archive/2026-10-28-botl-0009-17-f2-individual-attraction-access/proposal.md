# Proposal

## Why

Milestone F2-C06 lets individual tickets be used with `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` so that the ticket's user enters an attraction. The ticket must exist and be valid for the current system date (checked only at entry), and the attraction must have room. The access operation must rely on the common ticket abstraction rather than a type-specific mechanism.

## What Changes

- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier`: the attraction and ticket must exist, the ticket date must equal the current system date, there must be capacity for all users the ticket admits, and no admitted user may already be inside; then the user(s) are added to the attraction's occupants.
- Implement access once on the common ticket abstraction (users obtained from the ticket's individual tickets, all-or-nothing capacity check), so no separate per-type mechanism exists.
- Presence in an attraction does not depend on later system date changes.
- Extend `HELP`.
- Commit with the exact message `[F2-C06] Add individual attraction access`.

## Capabilities

### New Capabilities
- `attraction-access`: Entering attractions with tickets valid for the current date, subject to capacity.

### Modified Capabilities
- `execution-control`: HELP lists `ATTRACTION ENTER`.

## Impact

- Controller `enterAttraction` operation, `ATTRACTION ENTER` command, tests.
- **Flagged ambiguities:**
  - Group ticket access is milestone F2-C07. Because access is written once on the common abstraction (as the source requires), group tickets are structurally handled; their indivisible-access rules are specified and verified in F2-C07.
  - Only ticket identifiers of tickets created by `TICKET CREATE`/`GROUPTICKET CREATE` are accepted; an identifier of an individual ticket inside a group ticket's composition (`TG01:<dni>`) is not specified as usable on its own and is rejected.
  - The source defines no operation to leave an attraction and no rule preventing a user from being inside several attractions; only re-entering the same attraction while already inside is rejected (occupants are a set).
  - Output text is not specified: success prints `ATTRACTION ENTERED: <n> user(s) in <attraction>`.
