# Proposal

## Why

Milestone F2-C10 (Delivery 2 integration) requires every Delivery 1 and Delivery 2 feature to work together. `HELP` must show the full Delivery 2 syntax list, and the Delivery 2 technical restrictions must hold: object relationships in the required directions, the ticket abstraction, a single central manager, historical tickets kept, and `--fly` reusing the normal repositories. The JAR must run a phase-2 command file in normal and `--fly` mode.

## What Changes

- `HELP` displays exactly the Delivery 2 syntax list (**MODIFIED** `execution-control`), which renames some Delivery 1 placeholders, e.g. `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`.
- `App`'s command wiring moves into one reusable method, so the application and the integration tests register exactly the same commands.
- Add an end-to-end integration test of a full phase-2 flow (system date and price, groups, individual and group tickets with discounts, attractions with enter, exit, list and status, deletions with history), followed by a restart from persisted data and a `--fly` run that leaves nothing behind.
- Add automated checks for the Delivery 2 restrictions that can be verified: relationship directions (groups and attractions hold no tickets; tickets hold no attractions; group tickets compose individual tickets and hold their group), the common ticket abstraction, the single central-management instance, and `--fly` using the same `ParkStorage` with a different directory (**ADDED** to `application-runtime`).

### Known gap (deferred, not silently resolved)
- The source says relationships "must not be unnecessarily replaced by identifiers or strings". The user model and `USER` commands aren't in the code base yet (batch ordering: they come from later Delivery 1 milestones), so tickets, groups and attractions refer to users by e-mail through `UserDirectory`. Switching those to `User` object references is left to the user-model milestone. Until users exist, user-dependent commands can only succeed in tests that provide a user lookup.
- Mandated commit messages and submission logistics are process concerns. The message `[F2-C10] Complete delivery 2 integration` is recorded with this change's commit.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `execution-control`: `HELP` syntax list updated to the Delivery 2 list.
- `application-runtime`: adds the Delivery 2 technical restrictions.

## Impact

- `CommandProcessor.HELP_TEXT`
- `App` (extract `registerCommands`)
- New tests: `Delivery2IntegrationTest`, `Delivery2RestrictionsTest`
- Help assertions updated in existing tests
