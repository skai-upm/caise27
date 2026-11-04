# Proposal

## Why

Tickets can be bought but not used. The park needs attractions with a maximum capacity that visitors enter with an individual or a group ticket valid for the configured date, can leave, and whose live status can be queried.

## What Changes

- New attraction: identifier, name, maximum capacity and the collection of users currently present. `ATTRACTION CREATE;A01;Tornado;20`.
- `ATTRACTION ENTER;A01;T001` / `ATTRACTION ENTER;A01;TG01`: an individual ticket admits its user; a group ticket admits the users of its individual tickets. The ticket must be dated exactly the system date; capacity can never be exceeded; group entry is all-or-nothing.
- `ATTRACTION EXIT;A01;T001`: the users of that ticket leave and free capacity; the date is not checked on exit.
- `ATTRACTION STATUS;A01` shows identifier, name, maximum capacity, users present, available places and the present users, without distinguishing how they entered.
- Both ticket kinds are handled uniformly through polymorphism on the common ticket abstraction.
- Attraction definitions persist; every execution starts with all attractions empty.
- `HELP` lists the attraction commands; `uml/model.puml` reflects the new classes.
- Out of scope: attraction types, passes, reservations.

The source requests the change name `manage-attractions`; this batch uses the BOTL ID `botl-0008-8-f2-attraction-management-and-visitor-access`.

**Flagged for decision:** this source specifies `ATTRACTION EXIT;<IdAtraccion>;<IdTicket>`; the brief notes an earlier Delivery 2 document that used an email address. This change implements the ticket-identifier form stated by the authoritative source; see design.md Open Questions.

## Capabilities

### New Capabilities
- `attractions`: Attraction data, creation, ticket-based entry and exit, capacity and status.
- `attraction-file-persistence`: Storage of attraction definitions; occupancy is not persisted.

### Modified Capabilities
- `application-control`: `HELP` lists the four attraction commands.
- `uml-model`: the model includes attractions and the polymorphic access through tickets.

## Impact

- New classes `Attraction`, `AttractionManager`, `AttractionFileStore`; `Ticket` gains the polymorphic `getHolders()` implemented by `IndividualTicket` and `GroupTicket`; `TicketManager.findForAccess`; `CommandProcessor` dispatches attraction commands and removes deleted users from attractions.
- New data file `attractions.txt`; `uml/model.puml` updated.
