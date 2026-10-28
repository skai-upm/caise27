# Design

## Context

`ParkController` (singleton) owns `UserService`, date and price; `CommandProcessor` dispatches commands. Group tickets do not exist yet. See proposal.md.

## Goals / Non-Goals

**Goals:** group model and commands; lock rule implemented once as a derived check that F2-C04 will feed with real group tickets.
**Non-Goals:** group tickets, group persistence.

## Decisions

- **`model.Group`**: `id` (final), `name`, ordered `LinkedHashSet<String>` of member DNIs; `addMember/removeMember/hasMember/getMemberDnis`.
- **`repository.GroupRepository`**: `LinkedHashMap<String, Group>`.
- **`service.GroupService(GroupRepository, UserService, ActiveGroupTicketCheck)`**: `ActiveGroupTicketCheck` is a functional interface `boolean hasActiveGroupTicket(String groupId, LocalDate today)`, and the service asks it each time (no stored lock flag). `ParkController` supplies `(id, today) -> false` until group tickets exist, together with a date supplier.
- **Exception rename**: `UserOperationException` → `OperationException` (generic rejected-operation signal) to avoid user-specific naming in group/ticket services.
- **User deletion**: `ParkController.deleteUser` calls `UserService.deleteUser` then `GroupService.removeUserFromAllGroups(dni)`; the processor uses this method.

## Risks / Trade-offs

- [Lock behavior not observable end to end until F2-C04] → unit-tested with a stub check that reports active tickets.
