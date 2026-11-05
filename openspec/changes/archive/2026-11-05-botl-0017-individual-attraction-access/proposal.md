# Proposal

## Why

Milestone F2-C06 lets users actually use their individual tickets: `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` admits the ticket's user into an attraction when the ticket is valid for the current system date and there is room. The access mechanism must rely on the common ticket abstraction so that group tickets (F2-C07) use the same path.

## What Changes

- `ATTRACTION ENTER;<attraction>;<ticket>`: the ticket must exist and be valid for the current system date (checked only at entry); the attraction must exist and have room; the ticket's user enters and stays inside regardless of later date changes.
- Access is resolved through the common ticket abstraction (each ticket reports the users it admits), avoiding a type-specific mechanism.
- Rejections (unknown attraction/ticket, ticket not valid today, no capacity, user already inside) print `ERROR: ...` and change nothing.
- Out of scope (F2-C07): the specification and verification of group-ticket entry, `ATTRACTION EXIT`, `ATTRACTION STATUS`.

## Capabilities

### New Capabilities
- `attraction-access`: Entering attractions with tickets.

### Modified Capabilities
<!-- None -->

## Impact

- `Ticket` gains an abstract "users admitted" operation implemented by each ticket type; `AttractionService.enter`; `ATTRACTION ENTER` command and HELP line.
