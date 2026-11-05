# Design

## Context

Layers are `presentation` / `business` / `persistence`. Economic data lives in `ParkController` (tickets with prices). Users exist only through `UserDirectory`, and there are no passes yet. See proposal.md - Why.

## Decisions

- **Package `business.invoice`**:
  - `Invoice` is immutable: title, recipient lines (label/value), ordered sections of lines (text plus an optional amount), and the total.
  - `abstract class InvoiceBuilder` is the common abstraction. It has protected steps `recipient(label, value)`, `item(section, text, amount)` (counted in the total) and `detail(section, text)` (informational), plus the public `build()`. `build()` keeps sections in first-use order, drops sections with no lines, and sums only counted items.
  - `UserInvoiceBuilder` has public steps `forUser(email, dni)`, `addIndividualTicket(IndividualTicket)` and `addPass(String description, BigDecimal price)`. The pass step is used by the passes milestone.
  - `GroupInvoiceBuilder` has public steps `forGroup(Group)` and `addGroupTicket(GroupTicket)` (one counted item with the total, one detail per component).
  - Alternative rejected: one `Invoice` constructor with optional lists and nulls, which the source explicitly forbids.
- **Director** in `ParkController`:
  - `createUserInvoice(email)` checks the user exists and adds standalone `IndividualTicket`s of that user (`getContainingGroupTicket() == null`).
  - `createGroupInvoice(groupId)` adds `GroupTicket`s whose group is that group.
  - Unknown recipients raise `ParkException`.
- **Presentation** `InvoiceCommands` formats an `Invoice` as text: `INVOICE - <TYPE>`, recipient lines, uppercase section titles, items as `  <text>  <amount>`, details as `    - <text>`, and `TOTAL: <amount>`. Malformed input raises `InvalidCommandException`.
