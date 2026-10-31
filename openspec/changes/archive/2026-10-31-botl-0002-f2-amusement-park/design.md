# Design

## Context

F1 baseline (package `es.upm.amusementpark`): `App` (mode selection, stdin/file reading), `CommandProcessor` (single shared parser, `split(";", -1)`, exact parameter counts, `OK:`/`ERROR:` output), `UserManager` (rules + persistence after each change, rollback on save failure), `UserValidator` (no regex), `User` (final DNI), `UserField`, `UserRepository` (`users.txt`, `;`-separated). See proposal.md for motivation. Constraints carried over: Java 21, standard library only, no regex, no frameworks, no patterns introduced only for F3.

## Goals / Non-Goals

**Goals:**
- One place owning the system date and base price; every date rule receives that date.
- Ticket types handled polymorphically (entry, exit, refund, locking) without duplicated rules.
- Every operation all-or-nothing in memory and on disk.
- `--fly` implemented purely by choosing a different storage directory.

**Non-Goals:**
- Singleton, repositories behind interfaces, generic DAO frameworks.
- Persisting attraction occupancy, the system date or the base price.
- Ticket-holder restrictions not stated in the source (e.g. one ticket per user per day, a user being in several attractions).

## Decisions

### D1. Park controller owns date and price
New `ParkController` holds `systemDate` (initialised by `App` with `LocalDate.now()`, the only OS-date read) and `basePrice` (`BigDecimal 40.00`). It owns `UserManager`, `GroupManager`, `TicketManager` and `AttractionManager` and implements the cross-cutting rules (user deletion, group locking). Managers never read the clock: date-dependent methods take the date as a parameter supplied by the controller. *Alternative:* a static/global clock — rejected (hidden global state; Singleton explicitly not required).

### D2. Domain model
- `Group`: `id`, `name`, `List<User> members` (references to registered users).
- `Ticket` (abstract): `id`, `date`, `price`; abstract `getHolders()`; `isActiveOn(d)` (date ≥ d), `isValidOn(d)` (date = d), `isRefundableOn(d)` (date > d).
- `IndividualTicket extends Ticket`: `User user`.
- `GroupTicket extends Ticket`: `Group group` (aggregation) and `List<IndividualTicket> tickets` (composition); holders = constituents' users.
- `Attraction`: `id`, `name`, `capacity`, `List<User> visitors`.
Entry/exit, refunds and locking operate on `Ticket` via `getHolders()`/date predicates, so neither attractions nor the controller switch on ticket type except where the command names the type (`TICKET DELETE` vs `GROUPTICKET DELETE`).

### D3. Ticket identifiers (open question 3)
One identifier namespace across individual tickets, group tickets and constituents. Constituents are named `<groupTicketId>-<n>` (n = 1.. in member order at purchase). `GROUPTICKET CREATE` fails atomically if the group-ticket id or any constituent id is taken; `TICKET CREATE` fails if its id matches any existing ticket including constituents. *Rationale:* deterministic, readable, never conflicts silently. Constituents can be used with `ATTRACTION ENTER/EXIT` (they are individual tickets) but not with `TICKET DELETE` (refund goes through the owning group ticket).

### D4. Attraction exit identifier (open question 1)
Adopt the F2 source example `ATTRACTION EXIT;<attractionId>;<ticketId>` (the appendix is the authoritative input). Individual ticket → its user leaves; group ticket → its present constituent users leave. No date revalidation. The earlier `<Email>` form is not implemented.

### D5. UML presentation (open question 2)
The later, more restrictive rules win: no methods, no usage relationships except for classes with no incoming relationship (`App` is shown with `..>` dependencies), attributes only primitives/strings/dates/decimals/enums, no visibility or other technical labels beyond `name : Type`, relationships labelled with the holding field name, `x` on the holding end (`A x--> B : field`), composition `*-->` and aggregation `o-->` where the owning end is already marked by the diamond. The diagram is written after implementation from the actual fields.

### D6. Historical references (open question 4)
Tickets keep a direct `User` reference in memory. `tickets.txt` stores, per ticket, the holder's DNI plus a snapshot of email, name and surname. On load the holder is resolved by DNI to the registered user; if that user has been deleted, a detached `User` is rebuilt from the snapshot (empty password/phone/card) and is not registered. Group tickets store `groupId` and group name; a deleted group is rebuilt as a detached, member-less `Group`. User deletion rules are not weakened: only past tickets can survive their user.

### D7. Attraction lifecycle (open question 5)
`attractions.txt` stores `id;name;capacity` only. Visitors are runtime state; every start has empty attractions. A user deleted while present (possible only after the date moved past their ticket) is also removed from attractions.

### D8. Persistence layout
`StoragePaths(dir)` resolves `users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt` in one directory. Normal mode uses the working directory (so F1's `users.txt` location is unchanged); `--fly` uses `tmp/<yyyyMMdd_HHmmss>[_n]/`. A shared `TextFileStore` helper reads/writes `;`-separated records for all repositories (`UserRepository` is refactored onto it without format change).
- `groups.txt`: `id;name;dni1,dni2,...`
- `tickets.txt`: `id;date;price;ownerGroupTicketId;dni;email;name;surname` (owner empty for standalone tickets)
- `grouptickets.txt`: `id;date;price;groupId;groupName`
Each manager saves its files after every successful change and restores in-memory state if the save fails.

### D9. Pricing
`BigDecimal`, scale 2, `HALF_UP`. Group total = base × n × (1 − discount); constituent price = total ÷ n (scale 2). Output `OK TICKET CREATED -> 40.00` / `OK GROUP TICKET CREATED -> 108.00` exactly as in the source (no colon after `OK`, unlike F1 messages, to preserve the specified text). `SYSTEM PRICE` accepts non-negative decimals parsed with `new BigDecimal` (no regex).

### D10. Purchase dates
Tickets cannot be bought for a date earlier than the system date ("allowable date"); buying for today is allowed. Dates use `DateTimeFormatter.ofPattern("uuuu-MM-dd")` with `ResolverStyle.STRICT`.

### D11. Command processing and `App`
`CommandProcessor` now receives `ParkController`; dispatch is split into per-family handlers (user, system, group, ticket, group ticket, attraction) to keep each method small. `App.run(args, in, out, err, baseDir)`: a leading `--fly` selects temporary storage, the next argument (if any) is the command file; temporary storage is deleted in a `finally` block so `EXIT`, end of input and file errors all clean up.

## Risks / Trade-offs

- [Re-creating a deleted user's DNI or group id re-attaches historical records on restart] → Accepted: identity is the DNI / group id; only historical (past) records are affected, so no lock is reintroduced.
- [Detached historical `User` objects have empty password/card] → They are never registered or authenticated; documented.
- [Abnormal JVM termination (kill -9) leaves `tmp/` data] → Only controlled termination is required; `tmp/` is git-ignored.
- [Whole-file rewrites per change] → Acceptable volume; simplest correct approach.

## Migration Plan

Existing `users.txt` files remain valid. New files are created on first use; missing files mean empty collections.
