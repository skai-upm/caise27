# Proposal

## Why

Delivery 3 (F3) matures the park application's architecture and business: the flat package must become explicit presentation/business/persistence layers with Singleton controllers and injected repositories, passwords must stop being stored in plaintext, and new features are needed — on-demand invoices built with the Builder pattern, an extended help, personal Speed/Special passes, and attraction types that restrict which tickets they admit and how passes are consumed on entry.

## What Changes

- **[F3-C01] Layers**: reorganise code into `presentation`, `business` (model, controllers, invoices, security, repository interfaces) and `persistence` (text-file repositories) packages, plus the application entry point; command syntax and behaviour unchanged.
- **[F3-C02] Invoices**: `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<grupo>` assemble an invoice step by step through a Builder abstraction, including only applicable sections (direct individual tickets and personal passes for users; group tickets for groups) with item prices and a total; not persisted.
- **[F3-C03] Singleton + DI**: each controller has a single instance (Singleton); repositories are ordinary objects created at startup for the chosen data directory (normal or `--fly`) and injected, together with controller-to-controller dependencies.
- **[F3-C04] Help**: `HELP` keeps the concise syntax list; new `HELP EXTENDED` adds a description per command. The help builder receives its formatting behaviour by injection and both use the registered command list.
- **[F3-C05] Passwords** — **BREAKING (storage format)**: add the jBCrypt Maven dependency; store BCrypt hashes, verify passwords against them for `USER DELETE`/`USER UPDATE`, keep the E1 format rules; existing plaintext entries are migrated to hashes on load.
- **[F3-C06] Passes**: `SPEEDPASS CREATE;<entrada>;<1|10>`, `SPECIALPASS CREATE;<entrada>` for individual tickets (SpeedPass = 20% of the ticket price, Special Pass = ticket price, at most one existing Special Pass per ticket), purchases show the amount; `SPEEDPASS DELETE;<pase>` / `SPECIALPASS DELETE;<pase>` refund unused passes of future tickets; consumed passes stay.
- **[F3-C07] Attraction types**: GENERAL, INDIVIDUAL, GROUP, SPECIAL, created with `ATTRACTION CREATE;<id>;<nombre>;<capacidad>;<TIPO>` (the E2 syntax creates GENERAL). Access capabilities are interfaces and attractions are generic types bounded by them.
- **[F3-C08] Pass use on entry**: `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` consumes one SpeedPass per admitted ticket (atomic for groups); SPECIAL attractions admit individual tickets only, consume their Special Pass automatically, and reject SPEED; output reports consumed passes.
- **[F3-C09] Persistence and UML**: persist passes (price, ticket, consumed) and attraction types; ticket and group-ticket deletion cascade to passes without partial deletions; update `uml/model.puml`.
- **[F3-C10] Integration**: `mvn package` builds an executable JAR that bundles the BCrypt dependency; all E1/E2 commands, file/interactive modes and `--fly` keep working; HELP and HELP EXTENDED list every command.

## Capabilities

### New Capabilities
- `application-architecture`: Layered package structure, Singleton controllers and injected repositories/dependencies.
- `invoicing`: On-demand user and group invoices assembled with a Builder.
- `password-security`: BCrypt hashing and verification of user passwords.
- `personal-passes`: SpeedPasses and Special Passes: purchase, pricing, refund, consumption state, persistence and cascading deletion.

### Modified Capabilities
- `attractions`: Attraction persistence keeps the attraction type; new requirements for attraction types, typed creation and pass-based entry.
- `command-interface`: HELP lists the Delivery 3 commands; new HELP EXTENDED requirement.
- `user-management`: User persistence stores password hashes instead of plaintext.
- `project-build`: A runtime dependency (jBCrypt) is allowed and the executable JAR bundles it.

## Impact

- All Java sources move to new packages; `pom.xml` gains jBCrypt and the shade plugin; `Main-Class` becomes `es.upm.parque.App` (unchanged name, new wiring).
- New data file `passes.txt`; `attractions.txt` gains a type column (old 3-column lines load as GENERAL); `users.txt` holds hashes.
- Proposal asks for ten "mandatory commits" `[F3-C01]`…`[F3-C10]`; the BOTL batch produces one commit per proposal, so those labels are recorded in that single commit's message.
