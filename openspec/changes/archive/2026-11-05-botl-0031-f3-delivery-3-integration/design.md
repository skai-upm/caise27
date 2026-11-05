## Context

- `CommandCatalog` is the single source for `HELP` and `HELP EXTENDED`.
- After F3-C07 and F3-C08 it uses the bracketed syntaxes `ATTRACTION CREATE;…;<capacity>[;<type>]` and `ATTRACTION ENTER;…;<ticket>[;SPEED]`. Those do not contain the literal section 15 strings.
- The pass commands are listed after `GROUPTICKET DELETE`.
- `Delivery2IntegrationTest` shows the pattern for end-to-end runs through `App.execute(args, workDir, stdin, out)`.

## Goals / Non-Goals

**Goals:**
- HELP contains every literal section 15 syntax, together with the Delivery 1 and 2 syntaxes, in a consistent order.
- An end-to-end integration test covering the three deliveries in normal and `--fly` modes.
- Honest reporting of the Git-history constraint.

**Non-Goals:**
- New functional behaviour.

## Decisions

1. **Catalog order.** Delivery 1, then Delivery 2 (unchanged order). The Delivery 3 variants sit right after their Delivery 2 counterparts (`ATTRACTION CREATE` with type after `ATTRACTION CREATE`; `ATTRACTION ENTER …;SPEED` after `ATTRACTION ENTER`). Then the remaining attraction commands, then the pass commands in section 15 order, then the invoices, then `HELP`, `HELP EXTENDED` and `EXIT`. This preserves both the Delivery 2 relative order and the section 15 relative order.
2. **Purposes.**
   - The base forms keep their descriptions.
   - The type form explains the four types.
   - The SPEED form explains SpeedPass consumption and atomic group entry.
   - The plain entry mentions that SPECIAL attractions consume the Special Pass automatically.
3. **Integration test.**
   - It builds `INPUT_FASE3` from Delivery 1 and 2 commands plus the Delivery 3 features, with assertions on the responses.
   - It restarts and checks restored state: passwords verifiable by deletion, attraction types, consumed passes blocking reuse, and invoices.
   - It runs the same file with `--fly` and checks that the responses are equal, that no `tmp/` directory remains, and that the normal files are unchanged.

## Risks / Trade-offs

- The HELP text grows from 27 to 29 lines, and the existing tests that pin its size are updated.
