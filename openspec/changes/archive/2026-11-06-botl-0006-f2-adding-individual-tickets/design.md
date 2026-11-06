# Design

## Context

The central `ParkController` owns groups and has integration points for user existence (`userExists`) and group-ticket dates (`groupTicketDates`). There is still no user model or USER DELETE command: those milestones come later in this batch. Group tickets arrive in the next milestone.

## Goals / Non-Goals

**Goals:**
- An inheritance-based ticket abstraction that reuses common data and price behavior.
- One identifier uniqueness check, valid for every ticket type.
- A user-deletion check that the future USER DELETE command can call as-is.

**Non-Goals:**
- Group tickets and their composition (next milestone), and the USER DELETE command.

## Decisions

- **`abstract class Ticket`** (id, date, price; `isActiveOn(LocalDate)` is true when date >= today, and `isDeletableOn(LocalDate)` is true when date > today). Price is a `BigDecimal` fixed at construction, so later SYSTEM PRICE changes do not affect it. **`IndividualTicket extends Ticket`** adds the owner. An abstract class instead of an interface reuses the shared fields and price logic, as the source requires. The group-ticket subtype will extend it next.
- **Owner referenced by email**: no user model exists yet, and email is the user key used by every command. The ticket keeps the one-way reference and users never hold tickets. When the user model arrives, the reference can become a user object without changing behavior.
- **Single `LinkedHashMap<String, Ticket>`** in `ParkController` holds every ticket, so identifier uniqueness is one `containsKey` check (`requireUnusedTicketId`) that all ticket types reuse. `ticketsOf(email)` traverses this map.
- **User deletion**: `ParkController.deleteUserReferences(email)` checks first, then mutates. It throws if any of the user's individual tickets is active or any group containing the user is locked. Otherwise it removes the user from every group. USER DELETE (later milestone) will call this before deleting the user. Checking everything before mutating guarantees that a rejected deletion changes nothing.
- **Messages**: `TICKET CREATED. PRICE: <price>`, `TICKET DELETED`.

## Risks / Trade-offs

- [TICKET CREATE cannot succeed end-to-end until users can be registered] → Unit tests use the user lookup stub. The lookup is wired when users exist.
- [Email as owner key would break if emails become editable] → The user-modification milestone must update ticket owners or switch to object references. Noted for that milestone.
