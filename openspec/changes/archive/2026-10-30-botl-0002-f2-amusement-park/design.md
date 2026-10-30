# Design

## Context

Delivery 1 code lives in package `es.upm.amusementpark`: `App` (entry), `CommandRunner` (shared line loop, file opening), `CommandProcessor` (parse/dispatch), `UserService` (rules), `UserRepository` (users.txt, write-through), `User`, `UserField`, `UserValidator`. `UserService` currently receives a `UserRepository`; USER DELETE knows nothing about tickets or groups. Motivation: proposal.md. Behaviour: specs.

## Goals / Non-Goals

**Goals:**
- One central controller (singleton) owning date, price and all repositories, so cross-cutting rules (user deletion vs tickets/groups, group locks) have one place to look.
- A ticket hierarchy where price and holders are polymorphic, so creation, deletion checks and attraction entry work on the abstraction.
- Repositories identical in normal and `--fly` mode; only the data directory changes.

**Non-Goals:**
- Layered packages (F3-C01), password hashing or other Delivery 3 features.
- Persisting system date, price or attraction occupants.

## Decisions

1. **`Park` singleton.** `Park.getInstance()` returns a lazily created single instance (private constructor). `open(Path dataDir)` (re)configures it: builds the four repositories on that directory, loads them in dependency order (users → groups → attractions → tickets), and resets date to `LocalDate.now()` and price to 40.00. Tests call `open` with a temp dir; there is still only one instance. *Alternative:* passing a context object everywhere — rejected, the source requires a single instance obtainable from anywhere.

2. **Services on top of the park.** `UserService`, `GroupService`, `TicketService`, `AttractionService`, `SystemService` hold no state and read everything from `Park.getInstance()`. Each throws `IllegalArgumentException` for rejections, as in Delivery 1. `CommandProcessor` dispatches to them. *Alternative:* one large `Park` class with every operation — rejected for size and readability.

3. **Ticket hierarchy.**
   - `abstract class Ticket`: `id`, `date`; `isActiveOn(d)` (`!date.isBefore(d)`), `isDeletableOn(d)` (`date.isAfter(d)`), `isValidOn(d)` (`date.equals(d)`); abstract `getPrice()` and `getHolders()`; shared `protected static BigDecimal discounted(BigDecimal base, int percent)`.
   - `IndividualTicket extends Ticket`: `User user`, `BigDecimal basePrice`, `int discountPercent` (0 when bought alone). `getPrice()` = `discounted(basePrice, discountPercent)`.
   - `GroupTicket extends Ticket`: `Group group` (nullable), `List<IndividualTicket> composition` (unmodifiable, built at purchase). `getPrice()` = sum of composition prices; `getHolders()` = composition users. `static int discountFor(int members)`.
   - Attraction entry uses `ticket.getHolders()` for both types: no type checks.

4. **Identifier space.** `TicketRepository.exists(id)` checks top-level tickets and every composed ticket. `find(id)` returns any ticket (top-level or composed). `isComposed(ticket)` tells whether an individual ticket belongs to a group ticket. Group ticket creation computes every id first, checks them all, and only then builds and stores the ticket, so nothing is created partially.

5. **Derived state.** `TicketRepository.ticketsOf(User)` traverses top-level individual tickets and compositions; `groupTicketsOf(Group)` filters group tickets by reference. Group lock = any `groupTicketsOf(g)` active on the current date. Users and groups hold no ticket references; attractions hold `List<User> occupants`.

6. **Money.** `BigDecimal` with scale 2, `RoundingMode.HALF_UP`. SYSTEM PRICE parsing validates characters manually (digits, one optional `.`, at most 2 decimals) before `new BigDecimal`. No regex.

7. **Dates.** `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`.

8. **Persistence files** (all `;`-separated, one record per line, write-through after every mutation):
   - `groups.txt`: `id;name;dni1,dni2,…` (members linked by DNI on load).
   - `attractions.txt`: `id;name;capacity`.
   - `tickets.txt`: `INDIVIDUAL;id;date;basePrice;discount;<user>` and `GROUP;id;date;groupId-or-empty;basePrice;discount` followed by one `MEMBER;groupTicketId;<user>` line per composed ticket. `<user>` = `status;dni;firstName;surnames;email` where status is `ACTIVE` if the user object is a registered user at save time, else `FORMER`. On load, `ACTIVE` links to the registered user with that DNI; `FORMER` (or a missing user) builds a detached `User` from the snapshot (empty password/phone/card). This keeps historical records of deleted users and prevents re-linking them to a new user with the same DNI.
   - Group tickets of a deleted group are saved with an empty group id and reload with a null group.

9. **`--fly`.** `App` interprets `args[0] == "--fly"`: creates `tmp/<yyyyMMdd_HHmmss>` (suffix `_n` if taken), runs exactly like normal mode with that directory as data directory, and in `finally` deletes the directory tree (and `tmp/` if left empty). The remaining argument (if any) is the command file. Directory handling lives in a small `DataDirectory` helper, not in `App`.

10. **HELP.** Replaced with the Delivery 2 list, one syntax per line, using the source's capitalisation.

11. **UML.** `uml/model.puml` written after implementation from the actual classes; a test checks every class and enum named in the diagram exists in the code.

## Risks / Trade-offs

- [Singleton + tests share state] → Every test calls `Park.getInstance().open(tempDir)` in `@BeforeEach`; Surefire runs test classes sequentially by default.
- [Snapshot users in tickets.txt duplicate some user data] → Needed so historical tickets of deleted users survive restarts, as the source requires.
- [Write-through rewrites whole files] → Fine for course-sized data.
- [Changing HELP capitalisation] → The new text is the Delivery 2 contract; Delivery 1 tests are updated.
