# Design

## Context

See proposal.md – Why. `Main.main` prints the banner, wires repository/service/`CommandProcessor` and calls `run(processor, BufferedReader, PrintStream)` over `System.in`.

## Goals / Non-Goals

**Goals:**
- One processing loop, two `BufferedReader` sources; no duplicated operation code.

**Non-Goals:**
- Echoing commands, multiple files, or reading stdin after the file ends.

## Decisions

- **Source selection in `Main`**: `args.length > 0` → `Files.newBufferedReader(Path.of(args[0]), UTF_8)`; otherwise `System.in`. Both are passed to the existing `run` loop, which is the only place lines meet `CommandProcessor`. This is input wiring (opening a stream), not persistence logic, so the `user-management` rule about file logic in the entry point is not affected; reading itself stays inside the loop.
- **Error handling**: `run` declares `IOException`; `main` catches `IOException` (covers `NoSuchFileException`, `AccessDeniedException`, reading a directory, malformed UTF-8) and prints `Error: cannot read command file <path>` (file mode) or `Error: cannot read commands` (stdin), then returns normally. Alternative: `System.exit(1)` — rejected: source asks for a controlled handling, and exiting abruptly would also complicate tests.
- **Banner**: printed in both modes before processing.

## Risks / Trade-offs

- [Exit status on unreadable file is 0] → the source only requires controlled handling; the message makes the failure visible.
