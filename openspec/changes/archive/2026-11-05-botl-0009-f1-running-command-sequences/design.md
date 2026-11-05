# Design

## Context

`App.main` builds the file-backed `UserRepository` and `CommandProcessor` and calls `App.run(processor, Reader, PrintStream)`, which loops over lines and prints non-null responses. Blank lines already return `null` from `CommandProcessor`.

## Goals / Non-Goals

**Goals:**
- One processing path shared by both sources (source §10: "without maintaining two separate implementations").

**Non-Goals:**
- EXIT handling. That is the next milestone, which will need the loop to stop early. The shared loop keeps that a single change.

## Decisions

- **Source selection in `main` only**: with `args.length > 0`, open `Files.newBufferedReader(Path.of(args[0]), UTF_8)`; otherwise wrap `System.in`. Both go to the existing `run`. No operation code is duplicated.
- **Controlled failure**: an `IOException` from opening the file, or `NoSuchFileException`/`AccessDeniedException` when reading a directory, is caught in `main`. `main` prints `ERROR: cannot read command file '<path>'` and returns. It does not call `System.exit`: returning ends the JVM with status 0 and no stack trace, and avoids the exit-method code smell. The repository is created before the file is opened, but it only *loads* users, so nothing is modified.
- **Extra arguments are ignored**: the source defines only the first argument.
- **Reader closed with try-with-resources.**

## Risks / Trade-offs

- [Exit status 0 on a missing file may hide the error from scripts] → The `ERROR` line is printed. The source only requires "controlled" handling, and a non-zero status can be added later without changing behavior otherwise.
