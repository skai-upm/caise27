# Design

## Context

`App` reads lines and calls an empty `processLine`. `model.User` and `model.UserValidator` exist. Section 3.2 requires separate classes to represent users, run the application and store/retrieve users. See proposal.md.

## Goals / Non-Goals

**Goals:** a command layer that later milestones extend with new commands; clear split between parsing, business rules and storage.
**Non-Goals:** file persistence (F1-C04), other commands.

## Decisions

- **`cli.CommandProcessor`**: `String process(String line)` — splits with `split(";", -1)`, trims, dispatches on the first element via a `switch`; returns the text to print (or `null` for blank lines). Returning text keeps it unit-testable; `App` prints it.
- **`service.UserService`**: `createUser(...)` validates with `UserValidator`, checks uniqueness against the repository, stores the user; failures are signalled with `service.UserOperationException` carrying the error reason. The processor turns it into `ERROR: <reason>`.
- **`repository.UserRepository`**: in-memory `LinkedHashMap<String dni, User>` (keeps insertion order for later persistence), with `findByDni`, `findByEmail`, `save`, `findAll`. Alternative: list scan only — map gives direct DNI lookup and keeps order.
- **Messages**: `USER CREATED`; `ERROR: Unknown command`; `ERROR: Wrong number of arguments for USER CREATE`; `ERROR: Invalid DNI|email|password|card number`; `ERROR: A user with this DNI already exists`; `ERROR: A user with this email already exists`.
- **Wiring**: `App` constructs repository → service → processor (no DI framework; patterns not required in this delivery).

## Risks / Trade-offs

- [Message texts may need to change] → texts live in the processor/service only; tests assert prefixes and key words.
