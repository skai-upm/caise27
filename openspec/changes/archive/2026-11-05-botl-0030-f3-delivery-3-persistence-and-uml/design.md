## Context

- `ParkFileStore` writes `groups.txt`, `tickets.txt` and `attractions.txt` (with type), and loads them, counting malformed lines.
- `ParkRepositories.fileBacked` calls `store.load(users, groups, tickets, attractions)`, and `save()` calls `store.save(groups, tickets, attractions)`. Passes live in `PassRepository`, which is not persisted.
- `Pass` has a protected constructor with an explicit price. The public `SpeedPass` and `SpecialPass` constructors derive the price from the ticket.
- The UML already contains the Delivery 3 elements added milestone by milestone.

## Goals / Non-Goals

**Goals:**
- Persist and reconstruct passes: identity, ticket, stored price and consumed state.
- Validate stored passes.
- Make `--fly` include passes.
- Final UML, with automated checks for the section 14 items.

**Non-Goals:**
- Persisting occupants.
- Migrating old files (a missing `passes.txt` simply means no passes).

## Decisions

1. **Format.** One `passes.txt` file in the data directory: `S|P;id;ticketId;price;true|false`.
   - The ticket is referenced by identifier and resolved through the ticket repository, so passes on group components (`TG01:<dni>`) work.
   - The price is stored as text with 2 decimals and restored as stored, not recalculated.
2. **API.**
   - `ParkFileStore.load(users, groups, tickets, attractions, passes)` loads passes last, because they need tickets.
   - `ParkFileStore.save(groups, tickets, attractions, passes)`.
   - `ParkRepositories` passes its `PassRepository` to both.
   - `PASSES_FILE = "passes.txt"`.
3. **Validation on load.** Each of these makes a line malformed (counted, skipped):
   - a line that does not have 5 fields;
   - a kind other than `S` or `P`;
   - an empty identifier, or an identifier already loaded;
   - a ticket that is missing or not an `IndividualTicket`;
   - a price that is invalid or not positive;
   - a state that is neither `true` nor `false`;
   - for `P`, a ticket that already has a Special Pass.

   A consumed pass is restored by calling `consume()`.
4. **Restore constructors.**
   - `SpeedPass(String id, IndividualTicket ticket, BigDecimal storedPrice)` and the `SpecialPass` equivalent are public; their Javadoc says they are for reconstruction.
   - The persistence layer may create model objects; it already creates tickets this way.
5. **UML.**
   - `ParkFileStore ..> PassRepository : loads/saves`.
   - The required-relationship test is extended to: the capability realizations; the four attraction subclasses; the pass-ticket cardinalities; `InvoiceBuilder <|-- UserInvoiceBuilder` and `GroupInvoiceBuilder`; the `ParkConfiguration` injection line; and `ParkController <<singleton>>`.

## Risks / Trade-offs

- Pass lines that reference a ticket missing from `tickets.txt` are dropped with a warning. This keeps the reconstructed state consistent.
