# Proposal

## Why

Milestone F2-C08: only users survive a restart today; groups, tickets (individual and group, with their relationships) and attractions are lost. The assignment requires all of them to be stored in text files and reconstructed at start-up — including historical tickets of deleted users or groups as fiscal records — and adds a temporary `--fly` mode for isolated, repeatable executions.

## What Changes

- Persist, in text files next to `users.txt`: groups and their members; individual tickets with their user; group tickets with their composition and, while the group exists, the group reference; attractions (without occupants).
- Reconstruct everything at start-up; attractions always start empty.
- Historical tickets keep enough user data (DNI, name, surnames, email) to be reconstructed after their user was deleted; group tickets of deleted groups are reconstructed without group reference but with identifier, date, price and composition.
- `--fly` as first argument (optionally followed by a command file): all repositories use files inside a new `tmp/<yyyyMMdd_HHmmss>/` directory, deleted when the execution ends (EXIT, end of input or end of the command file). Same repositories, configured with another directory. Without `--fly`, behavior is unchanged.
- No Java serialization, databases, frameworks or external libraries.

## Capabilities

### New Capabilities
- `park-persistence`: Persistence and reconstruction of groups, tickets and attractions, including historical records.

### Modified Capabilities
- `executable-application`: adds the `--fly` temporary persistence mode.

## Impact

- New storage interfaces/file implementations in `persistence`; repositories receive storages; `Park.start(Path dataDirectory)`; `App` argument parsing and temp-directory lifecycle; tests.
