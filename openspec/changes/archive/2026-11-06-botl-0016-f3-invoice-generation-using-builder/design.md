# Design

## Context

Tickets live in the `ParkController` map. Individual tickets record their owner email and whether they are part of a group ticket. Group tickets reference their group and composition. Users exist only through the `UserDirectory` port (existence + DNI). Layers: business must not depend on presentation or persistence.

## Goals / Non-Goals

**Goals:** a real Builder (common abstraction plus type-specific builders), automatic omission of empty sections, and totals derived from shown items.
**Non-Goals:** pass sections (passes do not exist yet), invoice persistence, numbering.

## Decisions

- **Package `es.upm.park.business.invoice`.**
- **Product `Invoice`**: immutable. It has a title, recipient lines, and an ordered list of `Section(title, List<Item>)`, where an `Item` has a description, an optional amount and detail lines. `total()` sums item amounts. `render()` produces the text. Items without an amount (composition details) never affect the total.
- **`abstract class InvoiceBuilder<B>`** (common abstraction, self-typed for fluent chaining) provides the shared steps `title`, `recipient(label, value)`, `startSection(title)`, `item(description, amount)`, `detail(text)`, and `build()`. `build()` drops sections without items and freezes the product.
- **`UserInvoiceBuilder`** adds `forUser(email, dni)` and `directTickets(List<IndividualTicket>)`. It ignores tickets flagged `partOfGroupTicket` defensively. **`GroupInvoiceBuilder`** adds `forGroup(Group)` and `groupTickets(List<GroupTicket>)`, which writes one priced item per group ticket and its composition as detail lines. The passes milestone will add `speedPasses(...)`/`specialPass(...)` steps to `UserInvoiceBuilder`.
- **Director**: `ParkController.userInvoice(email)` and `groupInvoice(id)` validate the recipient (`NotFoundException`), select the tickets by traversing the ticket map, and drive the builder. Invoices are generated from the current, persisted state and never stored.
- **Output**:
  ```
  INVOICE - USER
  User: ana@correo.com
  DNI: 12345678Z
  Individual tickets:
    T001 (2027-04-15) 40.00
  TOTAL: 40.00
  ```

## Risks / Trade-offs

- [The user invoice cannot be generated from the command line until users can be registered] → Same constraint as other user-dependent features. It is tested through the user directory stub.
