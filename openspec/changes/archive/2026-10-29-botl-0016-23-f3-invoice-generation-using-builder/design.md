# Design

## Context

Layers: `presentation`, `business.{model,control,exception}`, `persistence`. Tickets are kept by `ParkController`; group tickets reference their group. No passes exist yet.

## Goals / Non-Goals

**Goals:** Builder-based user and group invoices. **Non-Goals:** passes, invoice persistence.

## Decisions

- **Package `business.invoice`**:
  - `Invoice` (product): `title`, ordered `List<InvoiceSection>`, `total`; `toText()` renders it. Created only by builders.
  - `InvoiceSection`: title + `List<InvoiceLine>`; `InvoiceLine`: description, optional amount (composition lines have no amount, so they are not summed).
  - `InvoiceBuilder` (abstract, the common abstraction): `startInvoice(title)`, `addRecipient(lines...)`, `addItems(sectionTitle, List<Ticket>)` (common item step, sums prices), `build()` (computes total; omits sections without lines). Subclasses add only their meaningful steps.
  - `UserInvoiceBuilder`: `addUser(User)`, `addIndividualTickets(List<IndividualTicket>)`. The passes milestone adds its own pass step to this builder (no pass type exists yet, so no placeholder method is added).
  - `GroupInvoiceBuilder`: `addGroup(Group)`, `addGroupTickets(List<GroupTicket>)` (each ticket line with total price, followed by its composition lines without amounts).
  - `InvoiceDirector`: `userInvoice(User, tickets)` / `groupInvoice(Group, tickets)` select the relevant data and call only applicable steps.
- **`ParkController.createUserInvoice(email)` / `createGroupInvoice(id)`**: resolve recipient (not found → `ElementNotFoundException`), pick top-level `IndividualTicket`s of the user / `GroupTicket`s whose group is that group, delegate to the director.
- **Presentation**: `INVOICE CREATE` requires 2 arguments; target `USER`/`GROUP`, otherwise `InvalidCommandException`; prints `invoice.toText()`.

## Risks / Trade-offs

- [No passes step yet] → the user builder is the documented extension point for the passes milestone.
