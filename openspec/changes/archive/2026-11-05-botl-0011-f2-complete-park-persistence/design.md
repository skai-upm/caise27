# Design

## Context

All park state lives in the `ParkController` singleton. `App.run(args, stdin, out)` reads commands through `CommandProcessor`. Users aren't modeled yet. See proposal.md - Why.

## Goals / Non-Goals

**Goals:** a separate persistence layer with one repository per element type; a directory-based configuration so `--fly` reuses the same repositories; correct rebuilding of ticket↔group↔component relationships.

**Non-Goals:** user persistence (a later milestone, which adds its repository to the same directory configuration), persisting the date and price.

## Decisions

- **Package `es.upm.amusementpark.persistence`**:
  - `TextFile` handles line I/O. Writes are atomic: write to `<file>.tmp`, then `Files.move` with `REPLACE_EXISTING`.
  - `GroupRepository` (`groups.txt`), `TicketRepository` (`tickets.txt`) and `AttractionRepository` (`attractions.txt`) each take only their file `Path`.
  - `ParkStorage(Path dataDir)` composes them, with `load(park)` returning warnings and `save(park)`.
- **Formats** use `;`-separated fields, built and split with plain String operations. Command fields can never contain `;` because commands are themselves split on `;`.
  - groups: `id;name[;memberEmail...]`
  - attractions: `id;name;capacity`
  - tickets: `INDIVIDUAL;id;date;price;email`, and `GROUP;id;date;groupId;linked;componentCount` followed by `componentCount` lines of `COMPONENT;id;date;price;email`.
  - `linked` records whether the group reference still existed, so a later group reusing the identifier is never re-linked.
- **Load order**: attractions, then groups, then tickets, so group tickets can resolve their group. Rebuilding goes through public `ParkController.restore*` methods, which bypass business-rule checks (past dates and missing users are valid for historical records) but still enforce identifier uniqueness.
- **Save timing**: `CommandProcessor.setAfterCommand(Runnable)` runs after every non-blank processed line, including `EXIT`. Saving every time keeps data safe if the process is interrupted, and the files are small.
- **Arguments**: `[--fly] [file]`. `App.run(args, stdin, out, baseDir)` takes the base directory, which lets tests use temporary folders, while `main` uses the working directory. Normal mode uses `baseDir/data`. Fly mode creates `baseDir/tmp/<yyyyMMdd_HHmmss>` (with a `-N` suffix if it already exists) and deletes it recursively in `finally`.

## Risks / Trade-offs

- [Saving after every command rewrites the files] → acceptable at assignment scale, and it gives crash safety.
- [Restore methods are public] → documented as persistence-only. They still keep the identifier space consistent.
