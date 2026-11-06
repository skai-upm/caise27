# Design

## Context

See proposal.md – Why. `Group` stores `List<String> memberDnis`; `GroupService`, `TicketService.createGroupTicket` and `GroupFileStore` work with DNIs; `UmlModelTest` has a by-identifier exception for `Group.memberDnis`.

## Goals / Non-Goals

**Goals:**
- Object references for every in-memory domain relationship.

**Non-Goals:**
- Changing file formats (DNIs stay in `groups.txt`).

## Decisions

- **`Group`**: `List<User> members`; `addMember(User)` (false if the same DNI is already a member), `removeMember(User)`, `contains(User)` by DNI equality, `getMembers()` (unmodifiable) and derived `getMemberDnis()` for storage and messages. Since `USER UPDATE` mutates the same `User` object in place, members never need re-linking.
- **`GroupFileStore.load(Function<String, Optional<User>> userByDni)`**: resolves members; DNIs of users that no longer exist are dropped (user deletion already removes members, so this only cleans inconsistent files). `GroupRepository(GroupFileStore, lookup)`; `ParkController.start(Path, …)` loads users before groups.
- **`TicketService.createGroupTicket`** uses `group.getMembers()` directly.
- **UML**: `Group --> "0..*" User : members`; `UmlModelTest` drops `BY_IDENTIFIER`; the `User`-has-no-references check stays.
- **End-to-end test** in `MainTest`: a Delivery 2 command file run with `--fly` and without, comparing outputs and checking the temporary directory is gone.
