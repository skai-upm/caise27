# Design

## Decisions

- **`deleteUser`** runs all checks (existence, `verifyPassword`, `ensureUserDeletable`) before any change, then removes the user from groups and the registry, so failures never cause partial changes. Group removal reuses `removeUserFromAllGroups`.
- **Password check before the business guards**: an unauthenticated request learns nothing about the user's tickets.
- **`USER DELETE`** is added to `UserCommands` (3 fields). Errors flow through central exception handling.
