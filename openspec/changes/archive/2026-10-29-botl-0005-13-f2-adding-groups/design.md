# Design

## Context

`ParkController` (singleton) holds date and price; `CommandProcessor` dispatches on the first `;` field and maps `IllegalArgumentException` to `Error: <message>`. See proposal.md for the flagged prerequisites (users, group tickets).

## Goals / Non-Goals

**Goals:** group model and the four GROUP commands; lock rule as a computation.

**Non-Goals:** USER commands, user validation, group tickets, persistence.

## Decisions

- **`User`** (minimal): immutable-identity class with a private `email` field and getter; equality by email. Later user milestones extend it.
- **User directory in `ParkController`**: `Map<String, User>` keyed by email with `registerUser(User)` (programmatic, used by tests until USER CREATE exists) and `findUser(String)`. Central management "centralizes access to users", so this is its natural home.
- **`Group`**: private `identifier`, `name`, and a `LinkedHashSet<User>` members (insertion order, no duplicates). `addMember` returns false when already present; `removeMember` returns false when absent; `getMembers()` returns an unmodifiable view.
- **Groups in `ParkController`**: `LinkedHashMap<String, Group>`; operations `createGroup`, `addUserToGroup`, `removeUserFromGroup`, `deleteGroup` throw `IllegalArgumentException` with a descriptive message on rejection, so the processor prints `Error: ...`.
- **Lock rule**: `ParkController.hasActiveGroupTicket(Group)` evaluates the group tickets known to central management against `currentDate` (date ≥ current date). No ticket model exists yet, so the method is written over a ticket source that is currently empty and returns false; no boolean "locked" field is kept. `GROUP DELETE` uses the same rule (an associated ticket with date ≥ current date blocks deletion).
- **Test isolation**: `reset()` also clears users and groups.

## Risks / Trade-offs

- [Minimal `User` may need reshaping by the user-model milestone] → only email identity is fixed here, which every later rule also relies on.
- [Lock rule untestable with real tickets yet] → it is a single method that the ticket milestone will connect; tests cover the no-ticket behavior.
