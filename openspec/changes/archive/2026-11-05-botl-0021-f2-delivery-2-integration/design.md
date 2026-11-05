# Design

## Context

All Delivery 2 commands exist and are covered by per-feature tests. `HELP_TEXT` in `CommandProcessor` uses placeholder names chosen in earlier milestones. `App.execute(args, workDir, in, out)` already allows running the full entry point (including `--fly`) against a temporary working directory in tests. `TechnicalRestrictionsTest` and `UmlModelConsistencyTest` already guard dependencies, regex usage and diagram drift.

## Goals / Non-Goals

**Goals:**
- An exact §15 help listing, specified in one place.
- Executable evidence that the complete Delivery 2 flow works in both persistence modes.

**Non-Goals:**
- Changing response wording of other commands. The source allows any equivalent output.

## Decisions

- **`HELP_TEXT` lines copied verbatim from §15**, in its order, under an `Available commands:` header. The allowed `USER UPDATE` field names follow as an extra indented line, so the §15 line stays exact.
- **Spec consolidation** uses REMOVED with Reason/Migration for the seven per-capability HELP requirements, plus one MODIFIED `execution-control` requirement. This avoids keeping two sources of truth with conflicting placeholder spellings.
- **Integration test (`Delivery2IntegrationTest`)** writes an `input_fase2.txt` covering users, system date and price, groups, individual and group tickets with discount, attractions, entry and exit, status, the deletion rules and `HELP`/`EXIT`. It then:
  1. Runs it with `App.execute` in a temp work directory and checks key responses.
  2. Restarts in the same directory and checks the restored groups, tickets and empty attractions.
  3. Runs the same file with `--fly` in a fresh directory and checks the same responses with no data files and no `tmp/<ts>` left behind.
- **Structure test (`RelationshipDirectionTest`)** uses reflection over declared fields: `User` has no field whose type is `Ticket`, `Group` or `Attraction` (or collections of them), and `Group` has no `Ticket` fields. It also asserts that `IndividualTicket` and `GroupTicket` extend the abstract `Ticket`, and that `GroupTicket` holds a `List<IndividualTicket>`.

## Risks / Trade-offs

- [Older tests assert the previous placeholder spellings] → They are updated in this change to the §15 spellings.
