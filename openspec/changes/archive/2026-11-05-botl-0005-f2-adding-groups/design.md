# Design

## Context

`ParkController` (singleton) holds date and price; commands register into the shared `CommandProcessor`. Users and group tickets do not exist yet in the code base (batch ordering). See proposal.md - Why.

## Goals / Non-Goals

**Goals:** group rules enforced in one place (central management); lock state computed, never stored; no invented user/ticket models.

**Non-Goals:** user model, group tickets, persistence.

## Decisions

- **`Group`** (encapsulated: id, name, `LinkedHashSet<String>` member e-mails preserving insertion order, unmodifiable view). E-mails identify users because the source commands reference users by e-mail.
- **Ports in `ParkController`**: `UserDirectory` (`boolean exists(String email)`) and `GroupTicketDates` (`List<LocalDate> datesOf(String groupId)`), both functional interfaces with empty defaults, replaceable via setters. Later milestones (user registration, group tickets) plug their real stores in; tests inject fakes. Alternative — creating placeholder `User`/`GroupTicket` classes now — rejected: it would pre-empt those milestones' models.
- **`isGroupLocked(id)`** = any date in `datesOf(id)` with `!date.isBefore(currentDate)`; evaluated on each `GROUP ADD/REMOVE/DELETE`.
- **Errors**: domain operations throw `ParkException` (unchecked) with a user-facing message; `GroupCommands` prints `ERROR: <message>`. Success messages: `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`, `GROUP DELETED`.
- **`reset()`** additionally clears groups and restores default ports.

## Risks / Trade-offs

- [Until user registration exists, `GROUP ADD` always fails in the running application] → expected given ordering; covered by tests with an injected directory.
