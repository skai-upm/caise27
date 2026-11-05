# Proposal

## Why

Milestone F3-C02 adds on-demand invoices for users and groups, generated from the system's economic information (the persisted state loaded in memory) and built explicitly with the Builder pattern. An invoice includes only the sections that apply to its recipient and never double-counts group-ticket components.

## What Changes

- Add `INVOICE CREATE;USER;<email>`. The user must exist. The invoice shows the user's identifying information (e-mail, DNI) and the individual tickets bought directly for them, each with its price, and the total of the items shown. Component tickets of group tickets are not counted.
- Add `INVOICE CREATE;GROUP;<groupIdentifier>`. The group must exist. The invoice shows the group identifier and name, and the group tickets it bought, each with its total and its composition as uncounted detail lines. The total is the sum of the group-ticket totals. Members' personal purchases are not included.
- Add a Builder design:
  - a common abstract `InvoiceBuilder` with differentiated steps (recipient lines, counted items, uncounted details, build)
  - concrete `UserInvoiceBuilder` and `GroupInvoiceBuilder` with type-specific steps
  - a director in central management that decides which steps apply
  - an immutable `Invoice` result
  - no constructors with optional parameters or nulls
- Sections without information are omitted. Invoices are not persisted.
- `HELP` lists both `INVOICE CREATE` forms.

### Deferred
- SpeedPasses and Special Passes don't exist yet (a later Delivery 3 milestone, processed later in this batch). `UserInvoiceBuilder` exposes a pass step for that milestone to call. Until then no pass section can appear.
- Users exist only through `UserDirectory` (no user model yet), so identifying information is the e-mail and DNI it provides.

## Capabilities

### New Capabilities
- `invoices`: On-demand user and group invoices built from the system's economic information, with per-recipient sections and correct totals.

### Modified Capabilities
<!-- None: HELP requires "at least" its list; adding lines changes no requirement. -->

## Impact

- New `business.invoice` package (`Invoice`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`).
- `ParkController` director methods.
- `presentation.InvoiceCommands` (formatting) and `App` wiring.
- `HELP`, UML and tests.
