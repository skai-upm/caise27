# Design

## Context

`ParkController` centralises users, groups, date and price. Delivery 2 §16 constrains relations: tickets know users, users never know tickets; ticket types share an abstraction, especially price behaviour.

## Goals / Non-Goals

**Goals:** abstract ticket, individual ticket, shared identifier space, TICKET commands, USER DELETE rule.
**Non-Goals:** group tickets and discounts (F2-C04), persistence (F2-C08).

## Decisions

- **`abstract class Ticket`** with `final String id`, `final LocalDate date`, `getPrice()`, `isActive(today)` (`!date.isBefore(today)`), `isDeletable(today)` (`date.isAfter(today)`) and `abstract boolean involves(User)`. Price behaviour lives in the base: tickets store a `basePrice` captured at creation and expose `getPrice()`; subclasses (group tickets later) override how the price is derived. Abstract class chosen over interface so the shared state and rules are implemented once.
- **`IndividualTicket extends Ticket`** with `final User user`; `involves(u)` compares the user.
- **`TicketRepository`**: `LinkedHashMap<String, Ticket>`; `isIdAvailable(id)` is the single common identifier check used by every creation path.
- **`ParkController.hasActiveTicket(User)`** traverses all tickets via `involves` + `isActive(currentDate)`.
- **`TicketCommands`** class (like `GroupCommands`) for TICKET CREATE/DELETE; date parsing shared with SYSTEM DATE via a common `Dates.parse` helper in the command package.
- **Price display**: `BigDecimal.toPlainString()` of a scale-2 value.

## Risks / Trade-offs

- [Deleted users' historical tickets still reference the in-memory `User`] → intended: history is preserved; persistence of the minimum data is handled in F2-C08.
