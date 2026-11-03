# Design

## Context

`App.run(InputStream, PrintStream, Path)` currently reads `System.in` until EOF and passes each non-blank line to `CommandProcessor.process`, which handles `USER CREATE/DELETE/UPDATE`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** a single loop and a single processor for both input sources; `HELP`/`EXIT` handled by the processor like any other command.

**Non-Goals:** command-pattern classes, help registries or other structures for future deliveries; prompts or output changes for existing commands.

## Decisions

- **Mode selection in `App.main`**: `args.length == 0` → wrap `System.in`; otherwise open `Path.of(args[0])` with `Files.newBufferedReader` (UTF-8). Both produce a `BufferedReader` handed to the same `App.run(BufferedReader, PrintStream, Path usersFile)` loop. Extra arguments beyond the first are ignored (assumption; the source only defines the first).
- **Single processing loop**: `run` reads lines, skips blank ones, prints `processor.process(line)` and stops as soon as `processor.isExitRequested()` is true. EOF also ends the loop. So EXIT works identically in both modes and nothing after it is read.
- **HELP/EXIT in `CommandProcessor`**: the first `;`-token, trimmed and case-insensitive, equal to `HELP` or `EXIT` with no extra parameters. `HELP` returns a multi-line text built from one constant list of command syntaxes (easy to extend in later deliveries). `EXIT` sets the `exitRequested` flag and returns `OK: Bye`.
- **File errors**: before opening, a missing path, a directory or a non-readable file → `ERROR: Cannot read command file <path>: <reason>` and return. `IOException` while opening or reading (including undecodable content) is caught in `App` with the same message; no stack trace reaches the user. Commands already executed before a mid-file read error keep their effects (they were already persisted).
- `User`, `UserValidator`, `UserFileStorage`, `UserService` are not modified.

## Risks / Trade-offs

- [EXIT with parameters, e.g. `EXIT;now`] → treated as an error (wrong parameter count) and processing continues; keeps the syntax strict and consistent with the USER commands.
- [Mid-file read failure leaves earlier commands applied] → acceptable and matches interactive semantics.
