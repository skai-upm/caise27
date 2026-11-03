# Proposal: F3 — Amusement Park, Delivery 3

## Why

Delivery 2 lives in one flat package with a single controller and plain-text passwords. Delivery 3 asks for an explicitly layered design (presentation, business, persistence) with exceptions, Builder-based invoices, Singleton controllers with injected replaceable repositories, injectable help generation, BCrypt passwords, SpeedPasses and Special Passes, attraction types restricted through interfaces and bounded generics, atomic pass consumption on entry, and the matching persistence and UML (milestones F3-C01 to F3-C10). Delivery 1 and 2 behaviour must keep working except where Delivery 3 explicitly changes it.

## What Changes

- Reorganise code into `presentation`, `business` (with `controller`, `invoice` subpackages) and `persistence` packages; introduce a `ParkException` hierarchy handled by the command layer (F3-C01).
- `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>` built with the Builder pattern, including only applicable sections and never double-counting composed tickets or member passes (F3-C02).
- Singleton controllers (`ParkController`, `InvoiceController`); repositories become business-layer interfaces with text-file implementations created in `App` and injected; controller collaboration injected; no DI framework (F3-C03).
- `HELP EXTENDED` (syntax + purpose) next to `HELP`, produced by a help generator that receives its formatting strategy by injection (F3-C04).
- **BREAKING (persisted format):** passwords stored only as BCrypt hashes (jBCrypt 0.4 via Maven); authentication checks hashes; earlier plaintext files are not migrated (F3-C05).
- `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (1 or 10, ids `<purchase>:<n>`, 20 % of ticket price each), `SPECIALPASS CREATE;<identifier>;<individualTicket>` (ticket price, at most one per ticket), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds; ticket and group-ticket deletion cascade to passes (F3-C06).
- Attraction types GENERAL, INDIVIDUAL, GROUP, SPECIAL via `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`; the 3-parameter form and untyped stored data mean GENERAL (F3-C07).
- `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` consuming one SpeedPass per entering ticket atomically; SPECIAL attractions consume the Special Pass automatically and reject SPEED (F3-C08).
- Persistence of attraction types and passes (price, ticket, consumed state) in normal and `--fly` modes; updated `uml/model.puml` (F3-C09).
- HELP and HELP EXTENDED list all commands of the three deliveries; integration (F3-C10).

## Capabilities

### New Capabilities

- `layered-packages-and-exceptions`: package layering and exception-based error handling.
- `invoice-builders`: user and group invoices built with the Builder pattern.
- `singleton-controllers-and-injected-repositories`: unique controllers and injected, replaceable repositories.
- `extended-help`: `HELP EXTENDED` and injectable help generation.
- `bcrypt-passwords`: BCrypt protection and verification of passwords.
- `speed-and-special-passes`: purchase, pricing and refund of passes.
- `generic-attraction-types`: attraction types restricting accepted tickets through interfaces and bounded generics.
- `atomic-pass-consumption`: pass use and consumption on attraction entry.
- `pass-persistence-and-uml`: persistence of passes and attraction types, cascading deletions, Delivery-3 UML.
- `delivery-three-integration`: coexistence of all three deliveries under the Delivery-3 restrictions.

### Modified Capabilities

- `user-persistence`: the stored line keeps the format but holds the BCrypt hash instead of the plaintext password.
- `help-and-exit`: `HELP` lists the Delivery-3 commands too.
- `delivery-one-integration`: the "no external libraries / no encryption" restriction now allows the BCrypt dependency.
- `delivery-two-integration`: the "no external libraries" restriction now allows the BCrypt dependency.

## Impact

- Every source file moves package; tests move accordingly.
- `pom.xml`: `org.mindrot:jbcrypt:0.4` runtime dependency and `maven-shade-plugin` so `target/nombre-aplicacion.jar` stays directly executable.
- New `passes.txt`; `attractions.txt` gains a type column (old 3-column lines still load as GENERAL); `users.txt` stores hashes.
- `uml/model.puml` rewritten for the Delivery-3 design.
