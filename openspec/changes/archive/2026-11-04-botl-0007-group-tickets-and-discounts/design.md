# Design

## Context

`Ticket` is currently a concrete immutable individual ticket; `TicketService` owns the individual tickets (`tickets.txt`) and answers `hasCurrentOrFutureTicket(user, today)`; `CommandProcessor` owns date and price and performs cross-service checks (e.g. ticket check before `USER DELETE`). `GroupService` owns groups whose members are `User` references.

## Goals / Non-Goals

**Goals:** ticket specialisation with a shared base, frozen composition, discount brackets, locks evaluated against the controller's date, consistent persistence across two files.

**Non-Goals:** attraction admission, invoices, editing group tickets.

## Decisions

- **Hierarchy**: `abstract class Ticket { id, date; abstract BigDecimal getPrice(); getPriceText() }`; `IndividualTicket extends Ticket` (user, holderDni, price — the former `Ticket`); `GroupTicket extends Ticket` (`Group group` nullable for history after the group was deleted, `String groupId`, `BigDecimal unitPrice`, `BigDecimal total`, `List<IndividualTicket> constituents` unmodifiable). Alternative — an interface — rejected: the shared state (id, date) and price formatting belong in a base class, and the source asks for inheritance in the model.
- **Discount**: `static int discountPercent(int members)` in `GroupTicket` (`<3`→0, `<=5`→10, `<=10`→20, else 50); total = `unit × n × (100 − d) / 100`, scale 2 HALF_UP.
- **Service**: `TicketService(TicketRepository, GroupTicketRepository, UserService, GroupService)` keeps `individual` and `group` lists. `createGroupTicket(id, groupId, date, today, unitPrice)` validates, builds constituents in member order, saves individual file then group file (restoring the individual file if the second write fails), then swaps lists. `deleteGroupTicket(id, today)` removes the group ticket and its constituents the same way. `isGroupLocked(Group, today)`; `findAny(id)` for the shared identifier space; `delete(id, today)` rejects constituents.
- **Locks in the controller**: before `GROUP ADD/REMOVE/DELETE`, `CommandProcessor` resolves the group and rejects if `tickets.isGroupLocked(group, currentDate)`; `GroupService` stays ticket-agnostic (dependency direction tickets → groups → users).
- **User deletion**: unchanged code path — constituents are `IndividualTicket`s in the individual list, so `hasCurrentOrFutureTicket` already covers them.
- **Persistence**: `grouptickets.txt` line `Id;GroupId;date;unit;total;c1,c2`. Load order in `App`: users → groups → tickets (individual then group). Constituent ids not found are skipped with a warning; the group reference is `null` when the group no longer exists (history).

## Risks / Trade-offs

- [Two files per group ticket operation] → write individual file first, then group file; on failure of the second, rewrite the previous individual state and report `ERROR:`.
- [Rename `Ticket` → `IndividualTicket`] → mechanical; tests updated in the same task; `tickets.txt` format unchanged.
