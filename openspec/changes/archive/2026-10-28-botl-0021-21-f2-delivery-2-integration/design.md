# Design

## Context

`Group` stores member DNIs (`Set<String>`), resolved through `UserService` — a string-based relationship that section 16 disallows. `USER UPDATE` already mutates users in place (since F2-C03). See proposal.md.

## Goals / Non-Goals

**Goals:** object-reference membership; exact HELP; full integration verification and docs.
**Non-Goals:** new features.

## Decisions

- **`Group.members : LinkedHashSet<User>`** (identity semantics — `User` has no `equals` override, so one object per user); `addMember(User)`, `removeMember(User)`, `hasMember(User)`, `getMembers()`.
- **Persistence keeps DNIs** in `groups.txt`; `GroupFileStorage.load` receives a `dni → Optional<User>` resolver (unknown DNIs skipped with a warning).
- **`ParkController`**: `deleteUser`/`createGroupTicket` work with `User` objects directly.
- **HELP text** replaced by the exact Delivery 2 list (plus a hint line with the `<Field>` values).
- **Integration test** `DeliveryTwoIntegrationTest` (file run, restart, `--fly`).

## Risks / Trade-offs

- [Identity-based sets rely on one `User` object per user] → guaranteed by the repository and loaders, which resolve DNIs to the single stored object.
