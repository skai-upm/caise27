# Proposal

## Why

Milestone F2-C06 lets users actually use their tickets: `ATTRACTION ENTER` with an individual ticket valid for the current system date puts its user inside the attraction, within capacity.

## What Changes

- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` for individual tickets: the ticket must exist and be dated on the current system date; the attraction must exist and have a free place; the ticket's user enters.
- The date is checked only at entry; presence inside does not depend on later SYSTEM DATE changes.
- Access is built on the common ticket abstraction: every ticket type exposes the users it admits, so a single access mechanism serves all types (group tickets are enabled on it in F2-C07).
- Deleting a user (when allowed) also removes it from any attraction it is inside, keeping occupants consistent with registered users.
- Commit with the exact message `[F2-C06] Add individual attraction access`.

## Capabilities

### New Capabilities
- `attraction-access`: Entering attractions with tickets — validity, capacity and the common access mechanism.

### Modified Capabilities
<!-- none -->

## Impact

- `Ticket` gains `holders()` (implemented by `IndividualTicket` and `GroupTicket`); `AttractionCommands.enter`; `ParkController` removes deleted users from attractions; HELP lists ATTRACTION ENTER.
- Ambiguities flagged: "valid for the current system date" is read as "ticket date equals the current system date" (tickets are valid only on their date); a user already inside that attraction cannot enter it again; a ticket may be used for several entries on its date; entering one attraction does not prevent being inside another (not restricted by the source); group-ticket identifiers are rejected with a "not supported yet" error until F2-C07; response: `ENTERED <attraction>: <email>`.
