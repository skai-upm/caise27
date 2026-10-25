# Design

## Context

After F2 all classes live in `es.upm.parque`: model (`User`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`), managers (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`), the main controller `ParkController` (system date/price and every cross-entity rule, which constructs its managers and repositories itself), repositories over `TextFileStore`, `StoragePaths`, `CommandProcessor` (switch-based) and `App` (modes and `--fly`). Every operation returns `OK ...`/`ERROR ...`. See proposal.md for motivation and the delta specs for behavior.

## Goals / Non-Goals

**Goals:**
- Clear layers with the composition root as the only place that knows concrete repositories.
- Patterns applied where the delivery asks: Builder (invoices), Singleton (controllers), DI (repositories, collaborators, help format, password encoder, invoice builder factory), interfaces + bounded generics (attraction access).
- Keep every E1/E2 output message unchanged.

**Non-Goals:**
- Dependency-injection frameworks, reflection-based command discovery, persisted invoices.
- Changing E1/E2 command syntax.

## Decisions

### D1. Packages (F3-C01)
- `es.upm.parque`: `App` (entry point, argument parsing, `--fly` directory) and `ParkApplication` (composition root).
- `es.upm.parque.presentation`: `CommandProcessor`, `ConsoleRunner` (line loop), `presentation.command` (`Command`, `SimpleCommand`, `CommandRegistry`, `ParkCommands` which registers every command against the controllers), `presentation.help` (`HelpGenerator`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`).
- `es.upm.parque.business.model`: domain classes and interfaces; `business.controller`: Singleton controllers; `business.invoice`: invoice product, builder and director; `business.security`: `PasswordEncoder` + `BCryptPasswordEncoder`; `business.validation`: `UserValidator`; date and price parsing stay in `ParkController`.
- `es.upm.parque.persistence`: repository interfaces (`UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`), file implementations (`FileUserRepository`, …), `TextFileStore`, `StoragePaths`.
Repository interfaces sit in `persistence` together with their implementations; controllers depend only on the interfaces.

### D2. Singleton controllers and DI (F3-C03)
Controllers: `ParkController` (system date and ticket price — the F2 "main controller"), `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController`. Each has a private constructor, a static `getInstance()` and a `configure(...)` method receiving repositories and collaborating controllers, plus `load()` to (re)read its repository. `ParkApplication.start(StoragePaths, LocalDate, PasswordEncoder)` creates the repositories, obtains all instances, configures them (cyclic collaborations such as users ↔ tickets are possible because wiring happens after instantiation), loads data in dependency order (users → groups → tickets with passes → attractions) and builds the command registry and processor. Re-running `start` (tests, or a second run in the same JVM) reconfigures the same instances.
*Alternative:* constructor injection without Singletons — rejected because the delivery explicitly requires Singleton controllers.

### D3. Commands and help (F3-C04)
`Command` exposes keyword, syntax variants, description, accepted parameter counts and `execute(String[])`. `SimpleCommand` implements it with a lambda; `ParkCommands.register(registry, controllers…)` registers every command (E1–E3). `CommandProcessor` splits with `String.split(";", -1)`, looks up the keyword in the `CommandRegistry`, checks the parameter count (`ERROR Wrong number of parameters. Usage: …`) and executes. `HELP` and `HELP EXTENDED` are registered commands, each holding a `HelpGenerator(registry, formatter)` built at startup with `SyntaxHelpFormatter` or `ExtendedHelpFormatter` (strategy injected), so a new format is just another `HelpFormatter`. `EXIT` is registered with an action that tells the processor to finish.

### D4. Passwords (F3-C05)
`PasswordEncoder` (`hash`, `matches`, `isHash`) implemented by `BCryptPasswordEncoder(logRounds)` over `org.mindrot:jbcrypt:0.4` (production 10 rounds, tests 4). `User` stores `passwordHash`; `UserController` validates the plaintext with `UserValidator` before hashing, and on `load()` hashes any value that is not a BCrypt hash, then saves. The executable JAR bundles jBCrypt with `maven-shade-plugin`.

### D5. Model: access capabilities, generics and passes (F3-C06/C07/C08)
- Interfaces: `AccessCapability` (`getId`, `getDate`, `getUsers`, `getIndividualAccesses`), `IndividualAccess extends AccessCapability` (`getUser`, `findUnusedSpeedPass`, `getSpecialPass`), `GroupAccess extends AccessCapability` (`getGroup`). `Ticket` (abstract) implements `AccessCapability`; `IndividualTicket` implements `IndividualAccess`; `GroupTicket` implements `GroupAccess`.
- `Pass` (abstract: `id`, `price`, `consumed`) with `SpeedPass` and `SpecialPass`. `IndividualTicket` composes `speedPasses` (0..*) and `specialPass` (0..1) and keeps `specialPassPurchased` so a refunded Special Pass cannot be bought again. Prices: SpeedPass = 20 % of the ticket's stored price (HALF_UP, 2 decimals), Special Pass = ticket price. IDs `<ticket>-SP<n>` (n = highest existing + 1) and `<ticket>-SPECIAL`. Purchases require the ticket date ≥ system date; refunds require unused pass and ticket date > system date. Group tickets themselves cannot hold passes (only their internal tickets).
- `Attraction<T extends AccessCapability>` (abstract) holds `id`, `name`, `capacity`, `present`, and `Class<T> accessType`. `admit(AccessCapability)` returns the ticket typed as `T` or rejects it; `enter(T, boolean speed)` is a template method: `passesToConsume(T, speed)` → presence/capacity checks → add users → consume passes, so failures consume nothing. Default `passesToConsume` takes one unused SpeedPass per `getIndividualAccesses()` element when `speed` is set. Subclasses: `GeneralAttraction extends Attraction<AccessCapability>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (rejects SPEED, requires the unconsumed Special Pass via `IndividualAccess.getSpecialPass()`). `AttractionType` enum creates the right subclass. `AttractionController` uses a generic helper `<T extends AccessCapability> EntryResult enter(Attraction<T>, AccessCapability, boolean)`.
- Entry answer: `OK ENTERED <A> -> <n> user(s)` plus `; SpeedPass consumed: …` or `; Special Pass consumed: …` when applicable.

### D6. Invoices (F3-C02)
Product `Invoice` (header text, list of `InvoiceSection` with `InvoiceLine(description, amount)`, total, `render()`). `InvoiceBuilder` interface: `reset`, `buildHeader(recipientType, recipient, date)`, `buildIndividualTickets(List<IndividualTicket>)`, `buildPasses(List<Pass>)`, `buildGroupTickets(List<GroupTicket>)`, `getResult()`. `StandardInvoiceBuilder` adds a section only when its list is non-empty. `InvoiceDirector` orchestrates: user → header, directly bought tickets, passes of all the user's tickets; group → header, group tickets. `InvoiceController` receives a `Supplier<InvoiceBuilder>` and the director at configuration time. Answer: `OK INVOICE <TYPE> <recipient>` followed by the rendered lines.

### D7. Persistence (F3-C09)
- `users.txt` unchanged layout, password column = hash.
- `tickets.txt`: `id;date;price;userDni;groupTicketId;specialPassPurchased` (5-field lines load with `false`).
- `passes.txt`: `id;SPEED|SPECIAL;ticketId;price;consumed`, attached to individual or internal tickets on load.
- `attractions.txt`: `id;name;capacity;type` (3-field lines load as GENERAL).
- `FileTicketRepository.saveAll` writes passes, then tickets, then group tickets, so a cascade deletion removes children before parents. Deletions validate every rule first and only then mutate; on an I/O failure the in-memory state is restored and saved again.
- `StoragePaths` gains `passes()`, so `--fly` covers it automatically.

### D8. UML (F3-C09)
`uml/model.puml` is regenerated from the final code using the F2 notation rules, adding interfaces (`<<interface>>` realizations), the generic `Attraction<T extends AccessCapability>` with bound subclasses (`Attraction<IndividualAccess>` etc. via `<|--` labelled with the binding), the pass hierarchy, invoice builder/director/product and the layered packages.

## Risks / Trade-offs

- [Singletons make tests share state] → `ParkApplication.start` fully reconfigures and reloads every controller; tests run sequentially.
- [BCrypt slows tests] → Tests inject a 4-round encoder.
- [Pass ID reuse after refunds of the highest number] → Acceptable; IDs remain unique at any time.
- [Large refactor may change outputs] → E1/E2 tests are kept (moved) and an end-to-end script compares behavior.
