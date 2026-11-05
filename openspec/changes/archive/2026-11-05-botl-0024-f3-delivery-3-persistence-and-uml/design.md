# Design

## Decisions

- **Stored price in `Pass`**: a `private final BigDecimal price` is set by the constructor. `SpeedPass` and `SpecialPass` constructors compute it from the ticket, and a restore constructor accepts a stored price. `getPrice()` becomes concrete in `Pass`. Because the price is fixed at purchase, it doesn't change even if pricing rules change later, so the persisted value is the purchase record.
- **Format**: `TYPE;id;ticketId;price;consumed`. The loader accepts 5-field lines, and 4-field legacy lines derive the price from the ticket. `restorePass(id, ticketId, special, price, consumed)` takes a nullable price, where `null` means derive it.
- **Cascades** are already persisted because every command saves the whole state after it runs. A test locks this in.
- **UML**: adds explicit association edges with multiplicities between `IndividualTicket` and each pass type, in addition to `Pass --> IndividualTicket`. `UmlModelTest.keyRelationshipsAreDrawn` is extended with the Delivery 3 checklist.
