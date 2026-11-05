## Context

The application has a layered structure:
- `presentation` parses commands and formats responses;
- `business` holds the managers and `ParkController`, and throws `ParkException` subclasses;
- `business.model` holds the domain;
- `persistence` holds the repositories and file stores.

Economic data available today:
- `IndividualTicket` (price, user, date);
- `GroupTicket` (price = sum of the discounted component tickets; group, possibly detached; composition).

`ParkController.isPartOfGroupTicket(ticket)` tells component tickets apart from stand-alone ones. `ParkController.groupTicketsOf(group)` returns a group's tickets. Passes (SpeedPass and Special Pass) are not modelled yet; they come with F3-C05.

## Goals / Non-Goals

**Goals:**
- Apply the GoF Builder pattern explicitly: an abstract builder, concrete builders, a director and a product.
- Invoices contain only the relevant sections that have content.
- No double counting of group-ticket components.

**Non-Goals:**
- Persisting invoices.
- Implementing passes; this change only provides the builder step where they will plug in.
- Invoice numbering or taxes.

## Decisions

1. **Product (`business.model`).**
   - `Invoice` is immutable: title, recipient lines, a list of `InvoiceSection` and a total.
   - The total is computed from the items of the sections; the product never stores optional fields as `null`.
   - `InvoiceSection` has a heading and a list of `InvoiceItem`.
   - `InvoiceItem` has a description, an amount and a list of detail lines that carry no amount; group-ticket composition is shown as detail lines.
   - Only the builders create these objects: the constructors are package-visible factories invoked through `InvoiceBuilder` helpers. Alternative considered: a single `Invoice` constructor with optional lists, which the source explicitly rejects.
2. **Abstract builder `InvoiceBuilder` (`business`).**
   - Template steps: `buildRecipient()`, `buildPurchases()` and `buildPersonalPasses()`. The passes step defaults to "nothing to add", which suits groups, because the source says passes must not be added to the group invoice.
   - `getResult()` returns the built product.
   - Protected helpers `addRecipientLine(text)` and `addSection(heading, items)`. `addSection` ignores empty item lists, so empty sections are omitted in one place, not in each variant.
3. **Concrete builders.**
   - `UserInvoiceBuilder(User, ParkController)`:
     - Recipient: name, surnames, DNI and email.
     - Purchases: `tickets.individualTicketsOf(user)`, filtered with `!park.isPartOfGroupTicket(t)` and sorted by date and then identifier.
     - Passes: no section until passes exist; F3-C05 fills this step.
   - `GroupInvoiceBuilder(Group, ParkController)`:
     - Recipient: identifier and name.
     - Purchases: `park.groupTicketsOf(group)` sorted by date and identifier. Each item is "Group ticket <id> for <date> (<n> tickets, discount <d>%)" at the group ticket's price, with one detail line per component ("<holder full name> (<dni>) - ticket <componentId>, <unit price> EUR").
4. **Director `InvoiceManager`.**
   - `forUser(email)` resolves the user, or throws `NotFoundException`.
   - `forGroup(id)` resolves the group, or throws `NotFoundException`.
   - `construct(builder)` runs recipient → purchases → personal passes → `getResult()`, so both variants follow the same ordered steps without duplicated assembly code.
5. **Presentation `InvoiceCommands`.**
   - Validates `INVOICE CREATE;<USER|GROUP>;<key>` and throws `InvalidCommandException` otherwise.
   - Renders the product generically:

     ```
     OK: <title>
       <recipient lines...>
       <section heading>:
         <item description>  <amount> EUR
           - <detail>
       TOTAL: <total> EUR
     ```

   - Amounts use `toPlainString()` with 2 decimals, as in the existing responses.
6. **HELP** gains the two invoice syntaxes before `HELP` and `EXIT`. The existing tests that pin the list are updated.
7. **UML** gains the new classes:
   - Builder inheritance: `InvoiceBuilder <|-- UserInvoiceBuilder` and `<|-- GroupInvoiceBuilder`.
   - `InvoiceManager ..> InvoiceBuilder : directs`.
   - `InvoiceBuilder --> Invoice : result`.
   - `Invoice *--> InvoiceSection` and `InvoiceSection *--> InvoiceItem`.
   - The builders' associations to `User` and `Group`.

## Risks / Trade-offs

- The passes step is empty until F3-C05. This is mitigated by making it an explicit, documented hook that the next milestone fills.
- Tickets of a group ticket whose group was deleted (detached historical tickets) appear on no group invoice, because the group no longer exists. Their components still stay excluded from the user invoice, which is consistent with "already paid in the group ticket".
