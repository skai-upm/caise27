# Design

## Context

Delivery 1 code lives in package `es.upm.etsisi.park`:

- `User` and `UserValidator`, plus the `UserField` enum.
- `UserRepository`, writing `users.txt` in the working directory.
- `UserService`, which holds the in-memory user map and applies the rules.
- `CommandProcessor`, a `switch` on the first `;` token, shared by stdin and file input.
- `App`.

Every mutation is validated first, then applied, then saved. A failed save rolls the change back. See proposal.md for scope and specs/ for behavior.

## Goals / Non-Goals

**Goals:**
- One singleton `Park` that owns all state and every cross-entity rule: locks, deletion checks, entry.
- A ticket hierarchy that shares identifier, date and price behavior, with no duplicated logic.
- Keep the Delivery 1 command semantics, outputs and `users.txt` format unchanged.
- `--fly` uses the same repository classes and differs only in the directory passed to them.

**Non-Goals:**
- Layered packages, a command registry, or several controllers. Those arrive in Delivery 3.
- Persisting occupants or the runtime date and price. Both are explicitly transient.
- Concurrency, or safety against multiple processes.

## Decisions

1. **`Park` singleton replaces `UserService`.** The singleton uses a private constructor and a `static final` instance returned by `Park.getInstance()`. `start(ParkRepository, LocalDate today)` resets all state for an execution: it loads the files, sets the date to `today` and the price to 40.00. Tests call it with a temp directory, so the single-instance guarantee holds while tests stay isolated. `UserService`'s user rules move into `Park` unchanged, because user deletion now depends on tickets and groups, and the source puts "operations that relate these elements" in the central object. *Alternative:* keep `UserService` and add hooks. Rejected because deletion rules would then be split across two owners.
2. **Ticket hierarchy.** This uses the source §7.1 mechanism choice.
   - `abstract class Ticket`: fields `id` and `date`. Methods `isActiveOn(d)` (date ≥ d), `isValidOn(d)` (date == d), abstract `getPrice()` and abstract `getHolders()`. The shared price rule lives here as `protected static applyDiscount(base, percent)`, using `BigDecimal` with scale 2 and HALF_UP rounding.
   - `IndividualTicket extends Ticket`: `user`, `basePrice`, `discountPercent`. `getPrice() = applyDiscount(basePrice, discountPercent)`. Standalone tickets have discount 0.
   - `GroupTicket extends Ticket`: `group` (nullable after group deletion) plus a `final List<IndividualTicket> composition`, fixed at construction (composition). `getPrice()` is the sum of composition prices. `getHolders()` maps the composition to users, so there is no parallel user list. `static discountFor(int members)` holds the brackets.
3. **Shared identifier space.** `Park.tickets` holds top-level tickets (standalone individuals and group tickets) in a `LinkedHashMap`. `Park.findTicket(id)` and `idInUse(id)` check top-level ids and every group ticket's composition ids. This is one common check for all types. Group purchase computes every derived id `<id>:<dni>` and checks all of them, plus the group ticket id, before creating anything.
4. **Derived locks.** `isGroupLocked(g)` is true when any `GroupTicket` has `group == g` and `isActiveOn(today)`. `hasActiveTicket(u)` is true when any top-level ticket has `isActiveOn(today)` and `u` among its holders. Nothing is stored.
5. **User deletion.** Steps: authenticate, reject if `hasActiveTicket`, reject if any group containing the user is locked, then remove the user from all groups and from the user map, then persist. The `User` object stays referenced by historical tickets.
6. **Group deletion.** Reject if locked. Otherwise set `group = null` on that group's group tickets (all historical at this point), remove the group, and persist.
7. **Entry and exit.** `Attraction.enter(List<User>)` checks `occupants.size() + n <= capacity`, and that no user is already inside, before adding any user. This one method serves individual and group tickets through `Ticket.getHolders()`. `exit(email)` finds the occupant by its current email. Occupants are a `List<User>` that is never persisted.
8. **Persistence layout.** `ParkRepository(Path dir)` aggregates four repositories, one file each:
   - `users.txt`: unchanged Delivery 1 format.
   - `groups.txt`: `id;name;dni;dni;...`. Members are referenced by DNI, which is immutable.
   - `tickets.txt`:
     - `I;id;date;basePrice;discount;dni;email;firstName;surnames` for a standalone ticket.
     - `G;id;date;groupId|` for a group ticket, followed by one `C;` line per constituent with the same fields as `I`.
   - `attractions.txt`: `id;name;capacity`.
   - Holder snapshot (DNI, email, name, surnames) per ticket line: on load, a ticket links to the active `User` with that DNI. If there is none (the user was deleted), it gets a detached historical `User` built from the snapshot, with blank password, phone and card, which is not registered in the park. This is the "minimum information to reconstruct" from source §9.
   - Shared atomic file writing moves from `UserRepository` into a small `TextFile` helper, so Sonar duplication stays low.
   - Saves write every file each time. Mutations are rare and files are small, and this keeps holder snapshots current after `USER UPDATE`. All temp files are written before any is moved into place.
9. **Atomic commands.** Each mutating `Park` operation validates everything first, applies the change, then calls `persist()`. If the save fails, a per-operation undo `Runnable` restores the in-memory state, the same pattern as Delivery 1.
10. **`--fly`.** `App` treats `args[0] == "--fly"` as fly mode, with an optional `args[1]` command file. It creates `tmp/<yyyyMMdd_HHmmss>/`, adding `_<n>` if that name already exists, passes it to `ParkRepository`, and in a `finally` block deletes the directory recursively, and `tmp/` too if it is left empty. Without `--fly`, argument handling is unchanged: `args[0]` is the command file, and the directory is the working directory.
11. **Parsing and formats.**
    - Dates use `LocalDate.parse` with `ISO_LOCAL_DATE` under strict resolution, so `2027-02-30` is rejected.
    - Prices are checked by hand, as digits with an optional `.` and 1–2 decimals and a value > 0, then wrapped in `BigDecimal`. No regex.
    - Capacity must be a positive `int`.
    - Outputs: `SYSTEM DATE SET`, `SYSTEM PRICE SET`, `GROUP CREATED/ADDED/REMOVED/DELETED`, `TICKET CREATED <id> PRICE <x> EUR`, `TICKET DELETED`, `GROUPTICKET CREATED <id> MEMBERS <n> DISCOUNT <p>% TOTAL <x> EUR`, `GROUPTICKET DELETED`, `ATTRACTION CREATED`, `ENTERED <n>`, `EXITED`, a list of emails or `(empty)`, and a multi-line status.
12. **HELP text** uses the exact syntax tokens from source §15 (for example `<FirstName>`). This supersedes the Delivery 1 lower-case `<firstName>` tokens, recorded as a MODIFIED requirement.
13. **UML.** `uml/model.puml` is checked with the PlantUML jar already in the local Maven cache (`java -jar plantuml.jar -syntax`). It is a verification tool only, not a project dependency.

## Risks / Trade-offs

- **Mandatory milestone Git history (10 commits with exact messages).** This conflicts with the batch harness. The BOTL orchestrator owns Git history and makes one commit per proposal, so the `[F2-C01] …` to `[F2-C10] …` commits are not created separately. tasks.md still follows checkpoint order and verifies each one. Flagged, not silently resolved.
- **Same user inside the same attraction twice.** Not specified. Rejected, because occupancy is a set of present users. Whether a user may be inside two attractions at once is also not specified, and it is allowed.
- **Re-using a ticket the same day.** Not specified. Allowed, since the ticket is valid for the whole day and only the date is checked.
- **Entering with a constituent ticket (`TG01:<dni>`).** Not specified. Allowed through the common abstraction: the single member enters.
- **Deleted user still inside an attraction.** Possible only after the date moves past their ticket date. Not specified; they stay in the occupant list and can still leave with `ATTRACTION EXIT` by email.
- **Multiple group tickets for the same group.** Not restricted by the source, so allowed.
- **Cross-file atomicity.** Writing four files is not a single atomic operation. Mitigation: all temp files are written first, then moved. A crash between moves is the residual risk.
- **Detached historical users.** These hold blank password, phone and card. They are never registered, so they cannot authenticate or collide with uniqueness checks.
- **`tmp/` for fly mode is relative to the working directory**, like `users.txt`. The source gives `tmp/20260826_190315/` as the example.

## Migration Plan

Existing `users.txt` files load unchanged. The new files are created on the first mutation. Rollback means reverting the commit; extra files are ignored by Delivery 1.
