# Design

## Context

Layers: `presentation.CommandProcessor` → `business.controller` managers → `persistence`. `TicketManager` keeps standalone `IndividualTicket`s and `GroupTicket`s (internal tickets live only inside group tickets). `SystemDate` is the shared current date. `UmlModelTest` enforces the model diagram.

## Goals / Non-Goals

**Goals:**
- A textbook Builder: abstract builder interface, concrete builder, product, and a director that drives the steps.
- Invoice content rules from the proposal; exact decimal totals.

**Non-Goals:**
- Persisting invoices; passes (the pass section is added with passes in a later proposal).

## Decisions

- **Product**: `Invoice` (immutable: recipient kind/id/name, issue date, list of `InvoiceSection`, total) with `toText()` producing the layout of the spec. `InvoiceSection` (title, list of `InvoiceLine`), `InvoiceLine` (description, amount).
- **Builder**: interface `InvoiceBuilder` with step methods `recipient(kind, id, name)`, `issuedOn(date)`, `section(title)`, `line(description, amount)` (fluent, returning the builder) and `build()`. `StandardInvoiceBuilder` accumulates the steps, drops empty sections and sums line amounts into the total at `build()`. Lines added before any `section(...)` are rejected with `IllegalStateException` (programming error).
- **Director**: `InvoiceManager` (controller) decides which steps apply: user invoices add the `Individual tickets` section from `TicketManager.ticketsHeldBy(user)` (standalone tickets only, so group-generated tickets are excluded by construction); group invoices add `Group tickets` from `TicketManager.groupTicketsOf(group)` (by identity).
- **Builder supply**: `InvoiceManager` receives a `Supplier<InvoiceBuilder>` in its constructor (`StandardInvoiceBuilder::new` wired in `App`), so a fresh builder is used per invoice and other builders (formats) can be plugged in without changing the director.
- **Package**: invoice model classes in `business.model`; `InvoiceManager` in `business.controller`.
- **Command**: `INVOICE CREATE` takes exactly two parameters (type, identifier); type matched ignoring case.
- **UML / UmlModelTest**: the test parser learns `interface` blocks and realisation lines (`..|>`), checked against `getInterfaces()`.

## Risks / Trade-offs

- [Interface with a single implementation] → Justified by the explicit Builder requirement and by the injected supplier enabling alternative builders.
