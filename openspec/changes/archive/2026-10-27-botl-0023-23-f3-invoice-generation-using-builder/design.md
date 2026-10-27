# Design

## Context

Business rules live in `business.controller`; tickets are traversed through `TicketRepository`. Passes do not exist yet.

## Goals / Non-Goals

**Goals:** explicit Builder; director deciding which sections apply; text rendering in presentation.
**Non-Goals:** persisting invoices; pass sections (F3-C06 adds them through the same steps).

## Decisions

- **Product** `Invoice` (immutable): `type` (USER/GROUP), ordered recipient lines (`label`, `value`), list of `InvoiceSection` (title, `InvoiceItem`s), `total`. `InvoiceItem` = description, amount, detail lines.
- **Builder interface** `InvoiceBuilder`: `forRecipient(type)`, `recipient(label, value)`, `section(title)`, `item(description, amount)`, `detail(text)`, `build()`. `StandardInvoiceBuilder` accumulates parts and computes the total from items at `build()`; `section` opened without items is dropped, so omitted sections need no special handling by callers.
- **Director** `InvoiceController` (business): `userInvoice(email)` and `groupInvoice(groupId)` decide the steps from the park state; throws `NotFoundException` for unknown recipients.
- **Rendering** `InvoiceCommands` (presentation) turns the `Invoice` into text:
  ```
  INVOICE (USER)
  Name: Ana Garcia Lopez
  ...
  [Individual tickets]
    T001 (2027-04-15): 40.00 EUR
  TOTAL: 40.00 EUR
  ```
