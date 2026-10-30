# Proposal

## Why

Delivery 3 of the Amusement Park assignment (milestones F3-C01 to F3-C10) asks for a layered, pattern-based design (layered packages, exceptions, Builder, Singleton controllers with hand-written dependency injection), secured passwords (BCrypt), new purchasable SpeedPasses and Special Passes, typed attractions restricted through interfaces and bounded generics, invoices for users and groups, extended help, and persistence/UML updates. Delivery 1 and 2 behaviour must be preserved except where explicitly modified.

Source: `.botl/proposals/opus_5_setup_single_gpt/0003-3.F3-Amusement-Park/3.F3-Amusement-Park.md` (sections 3–16, 18, 20 and milestones 17.1–17.10).

## What Changes

- **F3-C01** Move code into `presentation`, `business` (model, controllers, validation, invoices, exceptions, security) and `persistence` packages. Abnormal situations are modelled as exceptions (business exception hierarchy plus a persistence exception) handled at the presentation level; no command terminates processing.
- **F3-C02** `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>` built step by step with a Builder (common abstract builder, user and group concrete builders), omitting empty sections and never double-charging composition tickets or member passes on group invoices.
- **F3-C03** Controllers with a unique responsibility become Singletons; repositories are plain objects created during application initialization (paths differ in `--fly`) and injected; controller collaborations (e.g. attractions → passes, users → password hasher) are injected.
- **F3-C04** `HELP EXTENDED` prints a description next to each command's syntax; the help generator receives its formatting behaviour by injection.
- **F3-C05** Add `org.mindrot:jbcrypt:0.4`; users file stores only BCrypt hashes; authenticated operations verify with BCrypt; changed passwords are re-hashed. The executable JAR bundles this dependency. **BREAKING**: user files from earlier deliveries (plain-text passwords) are not migrated.
- **F3-C06** `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (1 or 10, ids `<purchase>:<n>`, 20% of ticket price each), `SPECIALPASS CREATE;<identifier>;<individualTicket>` (price = ticket price, at most one per ticket), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds, and cascading pass deletion on `TICKET DELETE` / `GROUPTICKET DELETE`.
- **F3-C07** Attraction types `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` via `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`; the 4-field form still creates `GENERAL`. Ticket access capabilities as interfaces; attractions use bounded generics.
- **F3-C08** `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` consumes one SpeedPass per entering ticket atomically; `SPECIAL` attractions consume the ticket's Special Pass automatically and reject `SPEED` and group tickets.
- **F3-C09** Persist attraction type and passes (price, ticket, consumed state); update `uml/model.puml`.
- **F3-C10** `HELP` lists every command of the three deliveries, including the new syntax.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, refunds, consumption state and cascade deletion with tickets.
- `invoices`: User and group invoices built from the persisted state.
- `password-security`: BCrypt protection and verification of stored passwords.
- `application-architecture`: Layered packages, exception-based error handling, Singleton controllers and dependency injection.

### Modified Capabilities
- `attractions`: `Create attraction` gains a type; `Enter attraction with a ticket` gains type restrictions, SpeedPass use and Special Pass consumption.
- `tickets`: `Delete individual ticket` and `Delete group ticket` also delete associated passes.
- `park-persistence`: `Persist park elements` also stores attraction types and passes.
- `user-persistence`: `Text file format` stores the BCrypt hash instead of the password.
- `command-interface`: `Command syntax` and `HELP command` cover Delivery 3 commands; new `HELP EXTENDED` requirement.
- `design-model`: `PlantUML class model` must show the Delivery 3 elements.
- `application-packaging`: `Standard library only at runtime` now allows the BCrypt dependency, bundled in the executable JAR.

## Impact

- All production classes move packages (`es.upm.etsisi.amusementpark.{presentation,business,persistence}`); tests move accordingly. `App` stays in the root package as composition root, so the JAR entry point is unchanged.
- `pom.xml`: jbcrypt dependency, maven-shade-plugin to produce a self-contained `target/nombre-aplicacion.jar`.
- New runtime data file `passes.txt`; `attractions.txt` gains a type column (old 3-field lines load as GENERAL).
- Milestone commits `[F3-C01] … [F3-C10]` folded into one BOTL commit (listed in its body), as in earlier proposals.
