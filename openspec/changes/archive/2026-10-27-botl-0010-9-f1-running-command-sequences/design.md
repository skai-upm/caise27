# Design

## Context

`App.run` already chooses standard input or `Files.newBufferedReader(args[0])` and feeds the same `CommandRunner`/`CommandProcessor`; blank lines are skipped by the processor (added by F1-C08, applied earlier here because of batch ordering). Any `IOException` while opening the file currently prints a single generic message.

## Goals / Non-Goals

**Goals:**
- Distinguish nonexistent from unreadable files; formalise and test order, blank lines and mode equivalence.

**Non-Goals:**
- Additional arguments such as `--fly` (F2-C08).

## Decisions

- **Pre-checks in `App`:** `Files.exists` → "command file not found"; `Files.isRegularFile && Files.isReadable` → otherwise "command file cannot be read". An `IOException` during reading (e.g. invalid encoding) is still caught and reported as "cannot be read". Keeping checks in `App` leaves `CommandRunner` source-agnostic.
- **No echo of file commands:** output is the same in both modes, which is what "identical processing" requires.
- **Mandatory commit:** `[F1-C07] Add command file input` is created by the batch Git step after verification and archive. It is recorded after `[F1-C08]` in this repository's history because of the batch processing order chosen at the start of the batch.

## Risks / Trade-offs

- [Commit order F1-C08 before F1-C07 differs from the roadmap order] → Consequence of batch ordering already pushed; both mandatory commits exist with their exact messages.
