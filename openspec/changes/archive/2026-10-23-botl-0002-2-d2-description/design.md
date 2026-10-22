# Design

## Context

Delivery 1 code (package `es.upm.amusementpark`), as it stands:
- `model.User` and `model.UserField`, plus `validation.UserValidator`.
- `service.UserService`: an in-memory user map that commits a change only after `UserRepository.saveAll` succeeds, and signals failures with `UserException`.
- `persistence.FileUserRepository`, which writes `users.txt` through a temp file and a move.
- `command.CommandProcessor`, which dispatches with a `switch`; `command.CommandRunner` (the shared input loop); and `App`, which selects the mode and takes the `users.txt` path.

See proposal.md for motivation and the specs for behavior. Constraints carried over: Java standard library only, no regex, parsing with `split`. New constraints: one central controller instance, a ticket hierarchy with composition, one-directional references, no Java serialization, and `--fly` that only changes storage paths.

## Goals / Non-Goals

**Goals:**
- Keep Delivery 1 code and tests working, and extend rather than rewrite.
- Derive every lock (user, group) from tickets and the current date. Lock flags are never stored.
- Keep each model relationship in the direction the assignment states, so the PlantUML diagram can mirror fields one-to-one.

**Non-Goals:**
- Reproducing the mandatory `[F2-Cxx]` Git milestone commits. BOTL creates one commit per proposal; the task groups follow the milestones.
- Persisting attraction occupants (explicitly excluded).
- Concurrency or several processes sharing a data directory.

## Decisions

**Central controller: `service.Park` singleton.** It has a private constructor and a `private static final Park INSTANCE` returned by `getInstance()`. `open(ParkStore store, LocalDate today)` (re)loads all state from a store and resets the date and price. This lets `App` and tests reuse the single instance with different data directories without creating a second one. *Alternative considered:* dependency-injected instances. Rejected because the assignment requires a guaranteed single instance.
Park holds the `UserService`, `LinkedHashMap`s of groups, top-level tickets, and attractions, `today`, and `price` (a `BigDecimal`). It exposes one method per command. Operations that span entities, such as `deleteUser`, live here.

**Model.**
- `Group`: `id`, `name`, and `List<User> members` with no duplicates. It holds no ticket references.
- `abstract Ticket`: `id` and `date`; abstract `getPrice()` and `getHolders()`; shared `isActiveOn`, `isValidOn`, and `isDeletableOn`; the protected static `discounted(base, percent)`; and `formatPrice()`.
- `IndividualTicket extends Ticket`: `User user`, `BigDecimal basePrice`, and `int discountPercent`. Its price is `discounted(basePrice, discountPercent)`.
- `GroupTicket extends Ticket`: a nullable `Group group` and a `List<IndividualTicket> composition` (unmodifiable, owned). Its price is the sum of the composition's prices, and its holders are the composition's users, so there is no parallel user list.
- `GroupDiscount`: a static bracket lookup (`percentFor(int members)`).
- `Attraction`: `id`, `name`, `capacity`, and `List<User> occupants`. Users reference nothing.

All ticket rules (validity, lock checks, entry) go through the `Ticket` abstraction. Entry uses `ticket.getHolders()` for both kinds.

**Ticket lookup and the shared ID space.** `tickets` holds top-level tickets (standalone individual tickets and group tickets). `findTicket(id)` checks that map, then the compositions of group tickets. `isTicketIdTaken(id)` is the single uniqueness check used by both creation commands. Group ticket creation pre-computes every `<id>:<dni>` and checks them all (including against each other) before creating anything. `TICKET DELETE` distinguishes the three cases: a standalone ticket, a group ticket ("use GROUPTICKET DELETE"), and a component ("belongs to group ticket X").

**Active-ticket and lock checks.**
- `hasActiveTicket(user)`: any top-level ticket that is active on `today` with `getHolders().contains(user)`. Identity comparison is safe because a user is always one object.
- `isLocked(group)`: any `GroupTicket` with `group == g` that is active on `today`.

**USER DELETE.** `UserService.authenticate` is made public, and Park checks credentials, then `hasActiveTicket`, then membership in a locked group. It then removes the user from groups and attractions (recording positions for undo) and calls `UserService.delete`, which saves everything. If the save fails, the removals are undone. Historical tickets keep referencing the (now inactive) `User` object.

**Persistence: `persistence.ParkStore` plus one file repository per entity in a data directory.**
- `users.txt`: unchanged format, using the existing `FileUserRepository`.
- `groups.txt`: `id;name[;memberDni...]`.
- `attractions.txt`: `id;name;capacity`.
- `tickets.txt`: one line per ticket, in order.
  - `INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames` for a standalone ticket.
  - `GROUP;id;date;groupId-or-empty;componentCount`, followed by `componentCount` `MEMBER;…` lines in the `INDIVIDUAL` layout.

Each repository renders lines and parses lines. `ParkStore.saveAll(users, groups, tickets, attractions)` writes all four temp files first and then moves them into place, so a failed write never leaves files mixed between old and new states. Loading skips malformed lines, unknown references, and duplicates with a warning, as in Delivery 1.

**Holder snapshot in tickets.** The ticket stores the holder's DNI, email, and names: the minimal identity needed for a fiscal record, without the password or card. On load, the DNI resolves to the active user. Otherwise the ticket gets a shared "former user" `User` built from the snapshot, with empty credentials, which is never added to the user registry. *Alternative considered:* a separate `former_users.txt`. Rejected as a second registry with the same purpose. Trade-off: if a deleted user re-registers with the same DNI, their old tickets link to the new account after a restart. The same DNI means the same person, which is acceptable.

**Undo on failed save.** `Park.persist(Runnable undo)` saves via the store. On `IOException` it runs `undo` and throws `ParkException`. Each mutating command applies its change, then calls `persist` with an inverse action. User commands keep Delivery 1's commit-after-save path: `UserService`'s repository is a Park-provided adapter whose `saveAll(users)` writes users plus the current groups, tickets, and attractions.

**Exceptions.** A new checked `ParkException`, which `UserException` now extends. `CommandProcessor` catches `ParkException`.

**Command dispatch.** `CommandProcessor(Park)` uses a `LinkedHashMap<String, Command>` (record `Command(String syntax, int argumentCount, Handler handler)`). The keyword is the syntax prefix before the first `;`. HELP is generated from the same table, so syntax and help cannot drift apart. This replaces the growing `switch`.

**Parsing.**
- Dates: `LocalDate.parse` (ISO, strict; rejects `2027-02-30`).
- Price: a manual character check (digits, at most one `.`, at most 2 decimals) and then `new BigDecimal`, which must be > 0.
- Capacity: digits only, then `Integer.parseInt` with an overflow guard.

All of this is done without regex.

**Attraction entry rules.** The checks run in order: the ticket is valid on `today`, every holder is an active registered user, no holder is already inside any attraction, and `occupants + holders ≤ capacity`. Only then are all holders added. A component ticket `TGxx:dni` may also be used, admitting that member alone, because it is a ticket valid for its user. The "already inside any attraction" rule prevents a user being in two places at once. It is a reasonable reading that the assignment leaves open.

**`--fly`.** `App.run(args, in, out, err, workDir)` parses `--fly` only as the first argument. `TemporaryDirectory.create(workDir/tmp)` makes `yyyyMMdd_HHmmss`, adding `_1`, `_2`, … if needed. The same `ParkStore` class is pointed at that directory. A `finally` block deletes the directory recursively, and also removes `tmp/` if it was created and is now empty. `App.main` passes `Path.of("")`, the working directory.

**Output.**
- Confirmations: `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`, `GROUP DELETED`, `TICKET CREATED. Price: 40.00 EUR`, `TICKET DELETED`, `GROUPTICKET CREATED. Tickets: 3, discount: 10%, total: 108.00 EUR`, `GROUPTICKET DELETED`, `ATTRACTION CREATED`, `ATTRACTION ENTERED (3 users)`, `ATTRACTION EXITED`, `SYSTEM DATE SET TO 2027-04-15`, and `SYSTEM PRICE SET TO 45.50 EUR`.
- LIST prints one email per line, or `NO USERS IN ATTRACTION A02`. STATUS prints labelled lines.

**Source example interpretation.** As in Delivery 1, the example lines that wrap passwords (`Clave1` / `!`, `Marta4 %`) are typesetting artifacts. The intended passwords are `Clave1!` and `Marta4%`.

**UML.** `uml/model.puml` lists the `model` classes, `Park`, and `GroupDiscount`, with fields and the relationships above:
- composition `GroupTicket *-- "1..*" IndividualTicket`
- `IndividualTicket --> "1" User`
- `GroupTicket --> "0..1" Group`
- `Group --> "*" User`
- `Attraction --> "*" User`
- `Park` aggregating all of them

It is checked by a unit test that each diagram class exists and each named field is declared.

## Risks / Trade-offs

- [Park becomes a large class] → Keep only orchestration in Park. Domain rules (pricing, validity, capacity, membership) live in the model classes and are unit-tested there.
- [The singleton complicates test isolation] → `open()` fully resets state. Tests call it with a fresh temp directory before each case, and surefire runs tests sequentially.
- [Whole-file rewrite on every change] → Acceptable at this data size. Temp-then-move keeps the files consistent.
- [Moving the system date backwards can make historical tickets of deleted users "valid" again] → Entry requires every holder to be an active registered user, so such tickets cannot be used.
- [A user inside an attraction can be deleted after a date change] → `USER DELETE` also removes the user from attraction occupants.
