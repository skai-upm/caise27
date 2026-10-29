# Proposal

## Why

Milestone F2-C10 closes Delivery 2: all features must work together, the commands of both deliveries must coexist, and HELP must show at least the Delivery 2 command syntax listed in the source (§15). The current HELP output lists the same commands but with placeholder spellings that differ from the Delivery 2 list (for example `<firstName>` instead of `<FirstName>`, `ATTRACTION STATUS;<attraction>` instead of `ATTRACTION STATUS;<attractionIdentifier>`), and no end-to-end check exercises every Delivery 2 command together in both execution modes.

## What Changes

- HELP prints exactly the Delivery 2 minimum syntax, in the source order: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP`, `EXIT`.
- The HELP requirement is consolidated into one requirement whose scenario lists this set; the four per-feature HELP requirements are removed (their content is covered by the consolidated one).
- Integration verification: an end-to-end test drives a full Delivery 2 scenario (system date/price, groups, individual and group tickets, attraction entry/exit/status/list, deletions, lock rules) and checks reconstruction after restart, plus `--fly` runs with a command file leaving no data; the packaged JAR is run with `input_fase2.txt` in both modes.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `command-interface`: HELP shows the Delivery 2 minimum syntax; per-feature HELP requirements removed in favor of it.

## Impact

- `CommandProcessor.COMMAND_SYNTAX`; existing HELP tests updated to the new spellings; new integration test.

## Ambiguities and unmet prerequisites (flagged)

- **USER commands:** USER CREATE / DELETE / UPDATE belong to Delivery 1 milestones applied later in this batch; HELP lists them (as required) but they are still reported as unknown commands. Consequently, an `input_fase2.txt` driven only by commands cannot create users, and every user-dependent command fails in a controlled way; the full flow is verified with programmatically registered users.
- **Placeholder spelling:** Delivery 1 (F1-C08) listed USER syntax with lower-case placeholders; Delivery 2 §15 lists the same commands with capitalised placeholders. Delivery 2 extends Delivery 1, so the Delivery 2 spelling is used; the command syntax itself is unchanged.
- **Git history:** the mandatory F2 commit messages are recorded verbatim in the bodies of this batch's commits; their relative order follows the batch order, which matches F2-C01…F2-C10 except that F2 milestones are interleaved with Delivery 1 ones.
