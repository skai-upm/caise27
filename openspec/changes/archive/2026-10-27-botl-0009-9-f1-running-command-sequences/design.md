# Design

## Context

`Main` already reads stdin or a file through one `process(BufferedReader, ...)` loop into one `CommandProcessor`, so there is a single implementation of the operations. Blank lines currently yield `ERROR: Unknown command`.

## Goals / Non-Goals

**Goals:** blank-line policy; tests proving order and identical output for both modes.
**Non-Goals:** EXIT/HELP (F1-C08).

## Decisions

- **Blank-line check in `CommandProcessor.process`** (`line.trim().isEmpty()` → `null`, meaning "no output"): putting it in the processor rather than in `Main`'s loop keeps the rule identical for any input source.
- No change to the reading loop: order is preserved because lines are processed sequentially as read.
