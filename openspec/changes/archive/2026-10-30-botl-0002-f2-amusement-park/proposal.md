# Proposal

## Why

Delivery 1 (users, validation, persistence, command modes) is complete. Amusement Park Delivery 2 (milestones F2-C01 through F2-C10, source: `.botl/proposals/opus_4_setup_single_gpt/0002-2.F2-Amusement-Park/2.F2 - Amusement Park.md`) extends the application into a park: a single central controller with a system date and price, user groups, individual and group tickets, attractions with access control, full persistence of the new elements, a temporary `--fly` mode, and a PlantUML class model. This change delivers Delivery 2 as one coherent OpenSpec change.

## What Changes

- **F2-C01** Add a single-instance central park controller holding the users, groups, tickets and attractions plus the current system date (local date at start-up) and base price (40.00 at start-up). Add `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.
- **F2-C02** Add groups: `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, no duplicate members, add/remove only while the group has no active group ticket (lock derived, not stored).
- **F2-C03** Add the common ticket abstraction, `TICKET CREATE;<identifier>;<email>;<date>` (current price, no past dates, price displayed) and `TICKET DELETE;<identifier>` (only strictly future, never a ticket belonging to a group ticket). **MODIFIES** USER DELETE: rejected while the user has any active ticket.
- **F2-C04** Add `GROUPTICKET CREATE;<identifier>;<group>;<date>` (non-empty group, composition of `<id>:<dni>` individual tickets, all identifiers checked atomically in the shared identifier space, member-count discount 0/10/20/50 %, total displayed), `GROUPTICKET DELETE;<identifier>` (strictly future, composition deleted too) and `GROUP DELETE;Identifier` (no active group ticket; historical group tickets keep their data with a null group reference). **MODIFIES** USER DELETE: also covers composed tickets, rejected when the user belongs to a locked group, otherwise removes the user from their groups.
- **F2-C05** Add attractions: `ATTRACTION CREATE;Identifier;Name;Capacity` and read-only `ATTRACTION LIST;AttractionIdentifier`.
- **F2-C06 / F2-C07** Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` (ticket must exist and be dated on the current system date, all holders enter atomically within capacity, shared mechanism for all ticket types), `ATTRACTION EXIT;AttractionIdentifier;Email` and `ATTRACTION STATUS;AttractionIdentifier`.
- **F2-C08** Persist groups (with members), tickets (individual with user, group with composition and group reference) and attractions in text files without Java serialization; attractions start empty each run; historical tickets of deleted users/groups survive restarts. Add `--fly [file]`: all repositories use a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted at the end of execution.
- **F2-C09** Add `uml/model.puml` describing the implemented model.
- **F2-C10** Extend HELP with every Delivery 1 and Delivery 2 command; all commands coexist.

### Flagged ambiguities and decisions (not new business rules)

1. **Per-milestone Git commits.** As in Delivery 1, the source mandates ten commits with exact messages (`[F2-C01] …` to `[F2-C10] …`). The BOTL batch harness produces one commit per proposal file, so these milestone commits are **out of scope** and recorded as a known deviation.
2. **Valid monetary amount** for SYSTEM PRICE: digits, optionally followed by `.` and one or two decimal digits, strictly greater than zero (e.g. `45.50`, `45`). Prices are shown with two decimals.
3. **Dates** in SYSTEM DATE, TICKET CREATE and GROUPTICKET CREATE use the strict `yyyy-MM-dd` format; anything else is rejected.
4. **Discount rounding.** Each composed ticket's price is `base × (100 − discount%) / 100` rounded half-up to cents; the group total is the sum of those prices.
5. **TICKET DELETE on a group ticket identifier, or GROUPTICKET DELETE on an individual ticket identifier**, is rejected (each command applies to its own ticket type).
6. **ATTRACTION ENTER with a composed ticket identifier** (`TG01:12345678Z`) is accepted as an individual ticket: it exists in the shared identifier space and holds one user.
7. **A user already inside the target attraction** cannot enter it again; for a group ticket, the whole entry is rejected if any member is already inside (atomicity). The source does not say whether a user may be inside two different attractions; this is not restricted.
8. **ATTRACTION EXIT** for a user who is not inside the attraction is rejected with an error.
9. **Capacity** must be a positive whole number.
10. **Historical tickets of a deleted user** keep a snapshot of that user (DNI, first name, surnames, email). If a new user is later registered with the same DNI, the old tickets still refer to the former user, not to the new one.
11. **`--fly` directory collision.** If `tmp/<timestamp>` already exists (two runs in the same second), a numeric suffix is added. An empty `tmp/` parent left behind by the cleanup is also removed.
12. **Data file names**: `users.txt` (unchanged), `groups.txt`, `tickets.txt`, `attractions.txt`, all in the working directory in normal mode.
13. **Central controller vs Delivery 3 layering.** All classes stay in the single existing package; the layered reorganisation is a later milestone (F3-C01).

## Capabilities

### New Capabilities
- `park-state`: Single central park controller, current system date and base price, SYSTEM DATE and SYSTEM PRICE.
- `user-groups`: Group creation, membership changes, deletion and the derived lock from active group tickets.
- `tickets`: Common ticket abstraction, individual and group tickets, shared identifiers, pricing and discounts, deletion rules.
- `attractions`: Attraction creation, ENTER/EXIT access control with capacity, LIST and STATUS.
- `park-persistence`: Persistence of groups, tickets and attractions and the `--fly` temporary mode.
- `design-model`: PlantUML class model consistent with the code.

### Modified Capabilities
- `user-management`: USER DELETE now also depends on active tickets and group locks and removes the user from groups.
- `user-persistence`: The users file lives in the configured data directory (working directory, or the `--fly` temporary directory).
- `command-interface`: New commands recognised; HELP lists all Delivery 1 and 2 syntaxes; `--fly` accepted as first argument.

## Impact

- Code: new domain classes (park controller, group, ticket hierarchy, attraction), repositories, command handling; existing user operations refactored behind the central controller.
- Data: new files `groups.txt`, `tickets.txt`, `attractions.txt`; `users.txt` format unchanged. `tmp/` and the new data files ignored by Git.
- New file `uml/model.puml`.
- No new dependencies; standard library only.
