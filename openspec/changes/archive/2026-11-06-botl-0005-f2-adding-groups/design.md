# Design

## Context

Groups refer to "users who already exist in the system" and are locked by "active group tickets", but at this point the code has neither registered users nor group tickets. These come from milestones that the batch processes later. The design has to implement the group rules completely while depending only on narrow questions about those future features.

## Goals / Non-Goals

**Goals:**
- Full group behavior and rules now, with the lock derived (never stored).
- Narrow integration points that the user and group-ticket milestones fill in without changing group code.

**Non-Goals:**
- A user model or user registration, and group ticket creation or deletion.

## Decisions

- **`Group` model** (`es.upm.park.model.Group`): private fields `id`, `name` and an insertion-ordered `LinkedHashSet<String>` of member emails. The set enforces "at most once". The model exposes `addMember`/`removeMember`, which return whether membership changed, and an unmodifiable member view.
- **Members referenced by email**: email is the user key used by all USER and GROUP commands. Storing the email instead of a user object keeps groups independent of the not-yet-existing user model. A later milestone can switch to user references internally.
- **`ParkController` integration points**:
  - `Predicate<String> userExists`, defaulting to "no user exists", replaceable through `setUserExistsLookup`. The user-management milestone wires it to the user registry.
  - `Function<String, Collection<LocalDate>> groupTicketDates`, defaulting to "no tickets", replaceable through `setGroupTicketDatesLookup`. The group-ticket milestone wires it to its storage.
  - `isGroupLocked(id)` is computed as "any ticket date >= current date". GROUP DELETE uses the same condition, as the source states.
  Alternative: implement stub User/GroupTicket classes now. Rejected because it would pre-empt those milestones' models.
- **Group operations on the controller** (`createGroup`, `addUserToGroup`, `removeUserFromGroup`, `deleteGroup`) throw `IllegalArgumentException`/`IllegalStateException` with user-facing messages. The command layer turns them into `ERROR:` lines. Rules live in one place, independent of the input source.
- **Messages**: `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`, `GROUP DELETED`.

## Risks / Trade-offs

- [GROUP ADD cannot succeed from the command line until users can be registered] → Accepted consequence of milestone order. Unit tests use a stub lookup, and the lookup is wired as soon as users exist.
- [The lock rule cannot be triggered end-to-end until group tickets exist] → Unit tests drive it through a stub ticket-date lookup, so the rule is verified now.
