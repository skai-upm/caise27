# Proposal

## Why

Milestone F2-C08 requires the park state (groups, tickets and attractions with their relationships) to survive the application ending and to be rebuilt on the next run from text files. It also adds a temporary `--fly` mode that runs the same repositories against an isolated, timestamped directory that is deleted at the end. Today all park state is lost when the process exits.

## What Changes

- Add text-file persistence for groups (with members), individual tickets (with their user), group tickets (with their component tickets and, while the group exists, the link to it) and attractions (identity, name, capacity). It is loaded at startup and saved after every processed command. There is no Java serialization, database or external library.
- Attraction occupants are not persisted, so every attraction starts empty.
- Historical tickets whose group was deleted are rebuilt without a group link but keep their identifier, date, price and composition. Tickets keep their user's e-mail even if that user no longer exists.
- Persistence is kept separate from the domain: one repository class per element type, each set up only with its file path inside a data directory.
- Add the optional first argument `--fly`. It creates `tmp/<yyyyMMdd_HHmmss>/`, points every repository at that directory, and deletes it with all its files when the run ends, whether through `EXIT` or the normal end of input. Forms supported: `[--fly] [commandFile]`. Without `--fly`, arguments behave exactly as before.
- Normal mode stores data in `data/` under the working directory. `data/` and `tmp/` are ignored by Git.

### Out of scope / deferred
- User persistence (the user model and `USER` commands aren't in the code base yet; batch ordering). The `--fly` directory setup applies to every repository that exists, so user repositories will automatically be covered when added.
- Persisting the system date and price, which by definition reset on each run.

## Capabilities

### New Capabilities
- `park-persistence`: Text-file persistence of groups, tickets and attractions with their relationships, and how they are rebuilt at startup.

### Modified Capabilities
- `application-runtime`: adds the `--fly` temporary persistence mode to the start-up arguments.

## Impact

- New `persistence` package (repositories, `ParkStorage`).
- `ParkController` restore operations.
- `CommandProcessor` after-command hook.
- `App` argument parsing and data directory.
- `.gitignore` and tests.
