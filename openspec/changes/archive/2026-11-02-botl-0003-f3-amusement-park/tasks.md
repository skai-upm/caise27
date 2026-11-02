# Tasks

## 1. F3-C01 — Layered packages and exceptions

- [x] 1.1 Move classes into `presentation`, `business.*` and `persistence` packages, introduce the `BusinessException` hierarchy and `CommandSyntaxException`, and make `Park` depend on a business-owned `ParkRepository`; verify the full regression suite passes and a test asserts no presentation class imports persistence and no business class imports `java.nio.file`

## 2. F3-C02 — Invoice builders

- [x] 2.1 Add `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceController` and `INVOICE CREATE;USER|GROUP`; verify with tests for omitted sections, no double counting of constituents, members' passes excluded from group totals, and unknown user/group

## 3. F3-C03 — Singleton controllers and injection

- [x] 3.1 Split operations into singleton `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController` configured by `ApplicationContext`; verify with tests for same instance / private constructors and controllers working with an injected in-memory `ParkRepository`

## 4. F3-C04 — Extended help

- [x] 4.1 Add `CommandRegistry`, `HelpGenerator` with injected `SyntaxHelpFormatter` / `ExtendedHelpFormatter`, and `HELP EXTENDED`; verify with tests that both list the same commands and only extended adds descriptions

## 5. F3-C05 — BCrypt passwords

- [x] 5.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin, `PasswordHasher` / `BCryptPasswordHasher`, hash on create and password update, verify on delete/update; verify with tests that the users file lacks the plaintext, old/new password behaviour after change, invalid passwords rejected, and plaintext legacy values fail

## 6. F3-C06 — SpeedPasses and Special Passes

- [x] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass`, `PassController`, `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE`; verify with tests for prices (incl. discounted constituents and later price changes), quantity 1/10, atomic id conflicts, one Special Pass per ticket, and refund rules

## 7. F3-C07 — Generic attraction types

- [x] 7.1 Add access interfaces, `Attraction<T extends AccessTicket>` with General/Individual/Group/Special subclasses, `AttractionType`, and typed `ATTRACTION CREATE` (3-parameter = GENERAL); verify with tests for each type's accepted/rejected ticket kinds and unknown type

## 8. F3-C08 — Pass consumption on entry

- [x] 8.1 Add `;SPEED` entry and automatic Special Pass consumption with all-or-nothing group behaviour and undo on save failure; verify with tests for individual SPEED, missing SpeedPass, group with one member lacking a pass, special entry consuming the pass, missing/consumed Special Pass, and SPEED rejected at SPECIAL

## 9. F3-C09 — Persistence, cascades and UML

- [x] 9.1 Persist passes and attraction types (legacy lines → GENERAL) and cascade pass deletion on TICKET DELETE / GROUPTICKET DELETE; verify with restart tests and cascade tests
- [x] 9.2 Rewrite `uml/model.puml` for Delivery 3; verify with PlantUML `-syntax` and a code-to-diagram review

## 10. F3-C10 — Delivery 3 integration

- [x] 10.1 Extend HELP / HELP EXTENDED to all Delivery 1–3 commands; verify with a test asserting every required syntax line and a description for each command
- [x] 10.2 Run the full suite and `mvn package`, then run the JAR with a mixed `input_fase3.txt` in file and `--fly` file modes plus an interactive `--fly` session; verify outputs match, the stored password is a BCrypt hash, and no `tmp/<timestamp>` remains
