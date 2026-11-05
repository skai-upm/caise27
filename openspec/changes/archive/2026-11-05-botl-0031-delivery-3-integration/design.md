# Design

## Context

HELP renders one line per registered command; alternative forms (`;SPEED`, `INVOICE CREATE;GROUP`) currently live in the note text of the main line.

## Goals / Non-Goals

**Goals:** HELP lists every form on its own line in the assignment's order; integrated verification. **Non-Goals:** behavior changes.

## Decisions

- **Alternative syntaxes**: the registry keeps, per command, additional syntax lines (`alternative(name, syntax)`); `catalogue()` emits one `CommandInfo` per syntax, all sharing the command's purpose, so both help formats list them.
- **Registration order** adjusted: ATTRACTION commands before SPEEDPASS/SPECIALPASS (Delivery 3 list), which preserves the Delivery 2 relative order.
- **End-to-end test** with the date-independent scenario (dates computed from today) through `App.run`, comparing `--fly` and normal outputs; compliance test checks consumed passes remain after restart.

## Risks / Trade-offs

- None.
