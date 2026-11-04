# Design

## Context

`App.main` currently builds `UserFileStorage(args[0] or users.txt)` → `UserManager` → `CommandProcessor` and loops over `System.in`, printing `process(line)` for each line (blank lines yield `ERROR: empty command`). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One loop and one processor shared by both input sources; `EXIT` handled in that shared path.
- HELP text maintained in one place.

**Non-Goals:**
- Command abstractions/registries for future commands; prompts; changes to user model, validation or storage.

## Decisions

- **`CommandRunner` (new):** `run(BufferedReader in, PrintStream out)` reads lines, skips blank lines, prints `processor.process(line)`, and stops when `processor.isExitRequested()` or EOF. Both modes call it; only the `BufferedReader` differs (stdin vs `Files.newBufferedReader(path)`). Alternative of two loops in `App` rejected: the source forbids duplicated processing.
- **HELP/EXIT inside `CommandProcessor`:** keeps every command's interpretation in one class. `EXIT` sets an `exitRequested` flag and returns `OK: application terminated`. HELP output is built from a single `USAGE` list plus the `UserField` values, so new commands only extend the list. Exact match after `trim()`; `HELP;x`/`EXIT;x` are errors (consistent with the strict parameter counting of user commands).
- **Users file fixed to `users.txt`:** the first argument now means the command file. Removing the override is recorded as a breaking launch change; tests use the package-private `App.run(...)` overload that receives the users path.
- **File error handling in `App`:** before opening, check `Files.isRegularFile` and `Files.isReadable`; then open and run inside `try/catch (IOException | UncheckedIOException)`. Errors are printed to standard error as `ERROR: cannot read command file '<path>': <reason>`, and `main` exits with status 1 via `System.exit` only on failure. `App.run` returns the exit code so tests can assert it without terminating the JVM. A users-file read failure is handled the same way.
- **Extra arguments** beyond the first are ignored (source defines only the first).

## Risks / Trade-offs

- [Launch change: previous `java -jar app users.txt` now treats `users.txt` as commands] → Documented as BREAKING; the users file name is unchanged, so default runs keep their data.
- [Interactive blank lines no longer print an error] → Required for equivalent processing; recorded as a MODIFIED requirement.
