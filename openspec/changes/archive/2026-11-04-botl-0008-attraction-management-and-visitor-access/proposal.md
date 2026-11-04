# Proposal

## Why

Users, groups and tickets exist, but there is nothing to use tickets for. This change introduces the park's attractions and lets visitors enter them with individual or group tickets valid for the configured date, respecting capacity atomically, leave them, and query their live status.

## What Changes

- New `Attraction` with identifier, name, maximum capacity and the collection of users currently present.
- New commands:
  - `ATTRACTION CREATE;A01;Tornado;20` — unique identifier, non-blank name, positive integer capacity.
  - `ATTRACTION ENTER;A01;T001` / `ATTRACTION ENTER;A01;TG01` — admits the ticket's visitors (the owner of an individual ticket; the owners of a group ticket's constituent tickets). The ticket's date must equal the system date; capacity can never be exceeded; group entry is all-or-nothing.
  - `ATTRACTION EXIT;A01;T001` — the ticket's visitors leave and free their places (no date check).
  - `ATTRACTION STATUS;A01` — identifier, name, maximum capacity, users present, available places and the list of users present.
- Access is uniform: every `Ticket` exposes its visitors polymorphically, and the attraction handles any ticket through that single abstraction.
- Attraction definitions persist in `attractions.txt`; occupants do not — every execution starts with all attractions empty.
- `HELP` lists the four commands; `uml/model.puml` gains `Attraction`, its service/repository and associations.

## Capabilities

### New Capabilities
- `attractions`: Attraction data, `ATTRACTION CREATE`, and persistence of attraction definitions.
- `attraction-access`: `ATTRACTION ENTER`, `ATTRACTION EXIT` and `ATTRACTION STATUS`, with validity, capacity and atomicity rules.

### Modified Capabilities
- None.

## Impact

- **Code**: new `Attraction`, `AttractionRepository`, `AttractionService`; `Ticket` gains abstract `getVisitors()` implemented by `IndividualTicket` and `GroupTicket`; `CommandProcessor` (four commands, HELP); `App` wiring.
- **Data**: new `attractions.txt` (`Identificador;Nombre;Aforo`); existing files unchanged.
- **Out of scope**: attraction types, passes, reservations, deleting/editing attractions.
- **Open questions / assumptions**:
  - The brief notes that another document used an email for `ATTRACTION EXIT`; the authoritative source text uses the ticket identifier (`ATTRACTION EXIT;A01;T001`), which is implemented. Exiting with a group ticket makes its members leave.
  - Entry is rejected if any of the ticket's visitors is already inside that attraction (no double counting); a user may be inside different attractions (the source sets no rule).
  - Exit is rejected when none of the ticket's visitors is inside; otherwise those present leave.
  - A ticket can be used again after leaving (no single-use rule in the source).
  - The `ATTRACTION STATUS` layout is chosen by us, as the source allows.
