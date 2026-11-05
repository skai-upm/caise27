# Design

## Context

Both input modes run `InteractiveSession` with one `CommandProcessor`; the session loops until end of input.

## Goals / Non-Goals

**Goals:** EXIT/HELP handled once in the processor so both modes behave the same. **Non-Goals:** `System.exit` inside command handling.

## Decisions

- **Exit as processor state** (`isExitRequested()`), checked by the session after each line, instead of calling `System.exit`: the session returns normally, `App` returns status 0, files are closed and the users file has already been saved. Alternative (exception for control flow) rejected as less clear.
- **EXIT prints the acknowledgement `GOODBYE`** so the controlled termination is visible (message text not fixed by the source).
- **HELP text built from a constant** listing each command's syntax on its own line, including the modifiable field names for USER UPDATE.
- **Extra elements on HELP/EXIT rejected** with `ERROR: wrong number of arguments`, consistent with the other commands.

## Risks / Trade-offs

- [HELP text must be kept in sync with new commands] → later deliveries extend the same constant.
