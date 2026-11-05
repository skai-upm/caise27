# Design

## Context

`UserRepository` keeps users in memory; `UserService` and `CommandProcessor` use it. The assignment forbids file handling in `User` or `App`.

## Goals / Non-Goals

**Goals:** persistence behind the repository so services/commands are unchanged; deletion and update (next milestones) persist by reusing the same save path.

**Non-Goals:** incremental/append writes, concurrency, encryption.

## Decisions

- **`persistence.UserStorage` interface + `UserFileStorage` implementation** (`load(): List<User>`, `save(List<User>)`). `UserRepository` receives a storage, loads in its constructor and calls `save` after each mutation. A no-arg repository uses a no-op storage (in-memory), keeping unit tests file-free. Dependency direction: `repository → persistence → model`.
- **Rewrite the whole file on each change** (write to the target with `Files.write`, UTF-8) rather than append: deletions/updates in F1-C05/F1-C06 need full rewrites anyway; the user count is small.
- **File location `users.txt` in the working directory** (source does not name it); `App` builds the path so it can be changed in one place.
- **Malformed lines skipped on load** rather than aborting start-up — the application must not terminate unexpectedly (assumption; source is silent).
- `split(";", -1)` keeps empty trailing fields so field count checks are exact.

## Risks / Trade-offs

- [Crash during write could truncate the file] → acceptable for the course scope; could write to a temp file and move later.
- [Running tests/JAR in the repo root creates `users.txt`] → integration checks run in a temporary directory.
