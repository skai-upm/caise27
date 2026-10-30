# Proposal

## Why

Deliveries 1 and 2 are complete in a single flat package. Amusement Park Delivery 3 (milestones F3-C01 through F3-C10, source: `.botl/proposals/opus_4_setup_single_gpt/0003-3.F3-Amusement-Park/3.F3 - Amusement Park.md`) requires a layered architecture with exceptions, Singleton controllers with hand-written dependency injection, BCrypt-protected passwords, SpeedPasses and Special Passes, typed attractions restricted with interfaces and bounded generics, pass consumption on entry, invoices built with the Builder pattern, extended help, and updated persistence and UML. This change delivers Delivery 3 as one coherent OpenSpec change.

## What Changes

- **F3-C01** Reorganise the code into `presentation`, `business` and `persistence` packages under `es.upm.amusementpark` (with subpackages). The presentation layer no longer touches files; domain objects do not persist themselves. Introduce an exception hierarchy (`ParkException` and subtypes) used to report rejected operations, handled at the presentation level so command processing never stops unexpectedly.
- **F3-C02** Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`, built through an `InvoiceBuilder` abstraction with user and group concrete builders and a director. Only relevant, non-empty sections are included. Composed tickets are not counted on user invoices; personal passes are not counted on group invoices.
- **F3-C03** Make the controllers Singletons (central `Park` plus user, group, ticket, pass, attraction, invoice and system controllers). Repositories become interfaces with text-file implementations, created at start-up (normal or `--fly` paths) and injected; controller-to-controller collaborations are injected too. No DI framework.
- **F3-C04** Add `HELP EXTENDED` (syntax plus purpose of each command). Help text is produced by a generator that receives its formatting behaviour by injection.
- **F3-C05** Add the jBCrypt 0.4 Maven dependency; store only BCrypt hashes; verify USER DELETE / USER UPDATE passwords against the hash; re-hash on password change. The executable JAR bundles the dependency.
- **F3-C06** Add `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (1 or 10, ids `<purchase>:<n>`, 20 % of ticket price each, atomic), `SPECIALPASS CREATE;<identifier>;<individualTicket>` (at most one per ticket, price = ticket price), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds (unconsumed and ticket strictly future). TICKET DELETE and GROUPTICKET DELETE cascade to the passes of the deleted tickets.
- **F3-C07** Attraction types GENERAL, INDIVIDUAL, GROUP, SPECIAL via `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`; the 3-argument form still creates GENERAL. Ticket access capabilities are interfaces implemented by the ticket types; attractions are generic over a bounded access type.
- **F3-C08** `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` consumes one SpeedPass per admitted ticket atomically; SPECIAL attractions automatically consume the ticket's Special Pass and reject SPEED and group tickets. Output names consumed passes.
- **F3-C09** Persist attraction types and passes (ticket, price, consumed state); `--fly` covers them; update `uml/model.puml`.
- **F3-C10** HELP lists all commands of the three deliveries; everything works together.

### Flagged ambiguities and decisions (not new business rules)

1. **Per-milestone Git commits.** The ten mandated `[F3-Cxx] …` commits are not produced; BOTL creates one commit per proposal file (same deviation as Deliveries 1 and 2).
2. **Special Pass after a refund.** "At most one throughout its entire existence" is enforced against existing passes (consumed or not). A refunded Special Pass no longer exists in the system, so the ticket may buy another one.
3. **Pass identifier space** is shared by SpeedPasses and Special Passes (not with tickets).
4. **Passes on group tickets.** SPEEDPASS/SPECIALPASS CREATE require an individual ticket identifier; composed tickets (`TG01:<dni>`) qualify, group ticket identifiers are rejected.
5. **Consumed passes and user invoices.** Invoices list every existing pass purchase (consumed or not), since consumed passes remain as purchase records.
6. **Which SpeedPass is consumed.** The oldest unconsumed SpeedPass of the ticket (purchase order); its id is printed.
7. **Users file from Delivery 2** containing plain-text passwords is not migrated (allowed by the source); those users cannot authenticate.
8. **BCrypt cost.** Production uses jBCrypt's default (10). The hasher is injected, so tests may use a lower cost.
9. **Executable JAR.** To keep `java -jar target/amusement-park.jar` working with a runtime dependency, the JAR is built with maven-shade so that it includes jBCrypt.
10. **Invoice with no purchases.** Header, recipient and a total of 0.00 are shown; empty sections are omitted.
11. **Exception style.** Unchecked hierarchy rooted at `ParkException` (`InvalidDataException`, `NotFoundException`, `DuplicateException`, `AuthenticationException`, `OperationNotAllowedException`), plus `PersistenceException` for storage failures.

## Capabilities

### New Capabilities
- `application-architecture`: Layered packages, exception handling, Singleton controllers and dependency injection.
- `invoices`: User and group invoice generation with the Builder pattern.
- `password-security`: BCrypt-protected password storage and verification.
- `passes`: SpeedPass and Special Pass purchase, pricing, refund and consumption state.

### Modified Capabilities
- `attractions`: Typed attractions; entry restricted by type, with SpeedPass / Special Pass consumption.
- `tickets`: Ticket deletions cascade to associated passes.
- `park-persistence`: Persist attraction types and passes.
- `user-persistence`: Password column holds a BCrypt hash.
- `command-interface`: New commands; HELP covers all deliveries; HELP EXTENDED.
- `design-model`: UML updated for Delivery 3.
- `project-build`: BCrypt is the one permitted runtime dependency; the JAR bundles it.

## Impact

- All production classes move to new packages; tests are updated accordingly. The entry point remains `es.upm.amusementpark.App`.
- New dependency `org.mindrot:jbcrypt:0.4`; build adds maven-shade-plugin.
- New data file `passes.txt`; `attractions.txt` gains a type column (old lines read as GENERAL); `users.txt` stores hashes.
