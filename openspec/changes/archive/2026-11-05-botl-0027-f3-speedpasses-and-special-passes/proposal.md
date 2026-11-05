## Why

Delivery 3, milestone F3-C06 (source sections 9 to 9.5), adds two personal purchases associated with individual tickets, SpeedPasses and Special Passes. The milestone covers their prices, purchase commands, consumption state, refunds, and their removal together with the ticket they belong to.

## What Changes

- **Model** (`business.model`):
  - abstract `Pass`: identifier, price, `IndividualTicket` and a consumed flag;
  - subclasses `SpeedPass` (20% of the ticket price) and `SpecialPass` (100% of the ticket price);
  - prices come from the ticket's stored price, so group discounts carry over and `SYSTEM PRICE` is never applied retroactively.
- **Storage.** New `persistence.PassRepository`, one identifier space for both pass types. It is injected through `ParkRepositories` like the other repositories.
- **Business.** New `PassManager` singleton, injected by `ParkConfiguration`. It covers:
  - SpeedPass purchase: quantity 1 or 10, ids `<purchase>:<n>`, all ids checked first, total shown;
  - Special Pass purchase: at most one per ticket, consumed or not;
  - refunds, only for unconsumed passes of tickets dated strictly after the current date.
- **Cascades.**
  - `TICKET DELETE` also removes the ticket's passes.
  - `GROUPTICKET DELETE` removes the passes of the composition, then the composition, then the group ticket.
  - Every check runs before anything is removed, so nothing is partially deleted.
- **Commands.** `SPEEDPASS CREATE`, `SPECIALPASS CREATE`, `SPEEDPASS DELETE` and `SPECIALPASS DELETE`, handled by the new `PassCommands`. They are added to the help catalog and to HELP.
- **Invoices.** The user invoice's personal-passes builder step now adds SpeedPasses and Special Passes sections, covering passes on the user's group-ticket components too. Group invoices still exclude passes.
- **Scope note.** File persistence of passes belongs to milestone F3-C09 (proposal 30), whose source section 13 lists it. In this milestone the injected pass repository is in memory: passes do not survive a restart yet.
- **Assumptions recorded.**
  - Pass and ticket identifiers are independent spaces.
  - A refunded Special Pass no longer exists, so the ticket may buy one again; a consumed one blocks repurchase.
  - Pass consumption (the command that consumes passes) arrives with F3-C08 (proposal 29). This milestone provides the consumed state and enforces it in refunds.

## Capabilities

### New Capabilities
- `passes`: pass identity, prices, purchases, Special Pass uniqueness and refunds.

### Modified Capabilities
- `tickets`: individual ticket deletion cascades to its passes.
- `group-tickets`: group ticket deletion cascades to the passes of its composition, consistently.
- `invoices`: the user invoice shows SpeedPasses and Special Passes.
- `execution-control`: HELP lists the four pass commands.

## Impact

- `business.model`: new `Pass`, `SpeedPass` and `SpecialPass`.
- `business`: new `PassManager`; `TicketManager`, `ParkController`, `ParkConfiguration` and `UserInvoiceBuilder` change.
- `persistence`: new `PassRepository`; `ParkRepositories` changes.
- `presentation`: new `PassCommands`; `CommandProcessor` and `CommandCatalog` change.
- UML, tests and README.
