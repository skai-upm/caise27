# Proposal

## Why

Delivery 3 of the Amusement Park assignment (source `3.F3 - Amusement Park.md`, milestones F3-C01 … F3-C10) restructures the Delivery 2 application into explicit layers with controlled exceptions, singleton controllers and hand-written dependency injection, and adds invoices (Builder), extended help, BCrypt-protected passwords, SpeedPasses and Special Passes, and typed attractions whose accepted tickets are restricted with interfaces and bounded generics. Delivery 1 and 2 behaviour must keep working except where Delivery 3 explicitly changes it.

## What Changes

- **F3-C01** — Reorganise code into `presentation` (command reading, parsing, output, help), `business` (model, rules, controllers, validation, invoices, exceptions, repository interfaces) and `persistence` (file repositories, storage configuration) packages under `es.upm.amusementpark`, with the entry point/composition root in the root package. Introduce a business exception hierarchy used to signal and propagate abnormal situations; presentation catches them and reports `ERROR: …` without stopping command processing.
- **F3-C02** — `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>` built with the Builder pattern (common builder abstraction, user and group concrete builders, a director). User invoices: identity, directly purchased individual tickets, SpeedPasses, Special Passes, item prices and total; composition tickets are not charged again. Group invoices: identifier, name, group tickets with composition detail, their totals and the invoice total; members' passes are not included. Empty sections are omitted.
- **F3-C03** — Controllers with a unique responsibility (central `Park` facade plus system, user, group, ticket, pass, attraction and invoice controllers) are Singletons. Repositories are interfaces in the business layer, implemented by file repositories, created during initialisation (normal or `--fly` paths) and injected into controllers together with controller-to-controller collaborations. No DI framework.
- **F3-C04** — `HELP EXTENDED` shows each command's syntax with a short description. Help generation receives the formatting behaviour (strategy) by injection; `HELP` keeps showing syntax only.
- **F3-C05** — Add `org.mindrot:jbcrypt:0.4`. Passwords are validated with the Delivery 1 rules, then stored only as BCrypt hashes; `USER DELETE`/`USER UPDATE` verify against the hash; a password change stores a new hash. The executable JAR bundles the dependency so `java -jar` still works without extra configuration. **BREAKING:** `users.txt` no longer contains plain passwords; earlier files are not migrated.
- **F3-C06** — SpeedPasses (`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity 1 or 10, ids `<purchaseIdentifier>:<n>`, 20 % of the ticket price each) and Special Passes (`SPECIALPASS CREATE;<identifier>;<individualTicket>`, same price as the ticket, at most one per ticket). Refunds `SPEEDPASS DELETE;<id>` / `SPECIALPASS DELETE;<id>` only for unconsumed passes of tickets dated after today. Valid `TICKET DELETE` / `GROUPTICKET DELETE` also delete the associated passes.
- **F3-C07** — Attraction types `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` via `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`; the 4-field form still creates `GENERAL`. Ticket access capabilities are interfaces implemented by individual and group tickets; attractions are generic classes bounded by those interfaces.
- **F3-C08** — `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` consumes one SpeedPass per admitted ticket (atomically for groups); SPECIAL attractions automatically consume the ticket's unconsumed Special Pass and reject `SPEED` and group tickets. Output names the consumed passes.
- **F3-C09** — Persist attraction types, passes (ticket, price, consumed state) using the same repositories in normal and `--fly` modes; update `uml/model.puml` with interfaces, generics, attraction types, passes, builder, controllers and injected dependencies.
- **F3-C10** — Integrate; `HELP` shows the commands of all three deliveries including the Delivery 3 syntax list.

### Flagged ambiguities (recorded, not silently resolved)

1. **Mandatory milestone commits.** The ten `[F3-C0x]` commits cannot be produced individually under the batch orchestrator's one-commit-per-proposal rule; they are listed in the commit body and must be reconciled by the course owner.
2. **Special Pass after refund.** "At most one Special Pass throughout its entire existence" is read together with "deleting a pass represents refunding that purchase; the pass then ceases to be part of the system's persisted state": a refunded (deleted) Special Pass no longer counts, so a new one may be bought; a consumed one always blocks a second purchase.
3. **Passes on composition tickets.** Passes may be bought for composition tickets (`TG01:<dni>`), as the source allows. Using a group ticket identifier as `<individualTicket>` is rejected.
4. **Composition tickets at INDIVIDUAL/SPECIAL attractions.** A composition ticket is an individual ticket, so it is accepted by INDIVIDUAL and SPECIAL attractions (consistent with Delivery 2's decision); only the group ticket itself is rejected there.
5. **Pass prices.** SpeedPass = 20 % of the stored ticket price rounded half-up to cents; a pack of ten costs exactly ten times that unit price.
6. **SpeedPass selection.** When several unconsumed SpeedPasses exist, the earliest purchased one is consumed and named in the output.
7. **SPEED keyword and attraction types** are case-sensitive, exactly as written in the source; anything else in the fourth field of `ATTRACTION ENTER` is rejected.
8. **Invoice content.** Invoices include historical as well as active items, and both consumed and unconsumed passes (all are purchases). A group invoice lists only group tickets still linked to that group (tickets of a deleted group are not reachable by group identifier). An invoice with no purchases shows only its header and a 0.00 total.
9. **BCrypt cost and legacy files.** Runtime uses cost factor 10. Plain-text passwords from earlier `users.txt` files are not migrated and will fail verification. Historical (deleted) users rebuilt from ticket records carry no password.
10. **Packaging.** To keep `java -jar target/nombre-aplicacion.jar` working with a runtime dependency, the JAR is built as a self-contained (shaded) JAR.
11. **Messages** keep the `ERROR: <reason>` convention; new confirmations are short upper-case lines (e.g. `SPEEDPASS CREATED: 10 pass(es), total 80.00 EUR`).
12. **Duplicated source sections** (6.x, 9.x, 10.x, 12.x appear twice) are treated as one requirement each.

## Capabilities

### New Capabilities

- `application-architecture`: Layered packages, exception-based error handling, singleton controllers and dependency injection of repositories, collaborators and help behaviour.
- `invoices`: User and group invoice generation with the Builder pattern.
- `passes`: SpeedPass and Special Pass purchase, pricing, refunds, consumption state and cascade deletion with tickets.
- `password-protection`: BCrypt hashing and verification of user passwords.

### Modified Capabilities

- `attractions`: `Create attraction` gains the optional type; `Enter attraction with a ticket` gains type restrictions, `SPEED` and automatic Special Pass consumption.
- `command-interface`: `Command syntax` recognises the Delivery 3 commands; `HELP command` shows the Delivery 3 syntax; new `HELP EXTENDED` requirement.
- `park-persistence`: `Temporary execution mode` covers passes; new requirement for persisting attraction types and passes.
- `user-model`: `User attributes` holds the protected password instead of the plain one.
- `user-persistence`: `Text file format` stores the BCrypt hash.
- `project-build`: `Standard library only at runtime` allows the BCrypt dependency.
- `design-model`: `Model consistent with code` covers the Delivery 3 design elements.

## Impact

- Large package reorganisation (`command` → `presentation`, `model`/`service`/`validation` → `business.*`, repositories → `persistence` implementing `business.repository` interfaces); `Park` becomes a facade over injected singleton controllers.
- `pom.xml`: `jbcrypt` dependency and `maven-shade-plugin`.
- New runtime file `passes.txt`; `attractions.txt` gains a type column (old 3-column lines read as GENERAL); `users.txt` stores hashes.
- `uml/model.puml` rewritten for the final design.
