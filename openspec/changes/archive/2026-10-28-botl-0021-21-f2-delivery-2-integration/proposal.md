# Proposal

## Why

Milestone F2-C10 closes Delivery 2: all Phase 1 and Phase 2 commands must coexist, `HELP` must show the exact Delivery 2 syntax, and the technical restrictions must hold — in particular, in-memory domain relationships must be object references with the prescribed directions, not identifiers or strings.

## What Changes

- **Groups reference their member `User` objects** instead of DNIs (DNIs remain only in the file format). Since `USER UPDATE` modifies users in place, references stay valid.
- `HELP` prints exactly the Delivery 2 syntax list: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP`, `EXIT`.
- End-to-end Delivery 2 integration test (users, groups, tickets, group tickets, attractions, persistence restart, `--fly`) plus the Delivery 1 session still passing.
- Update `README.md` (Delivery 2 commands, data files, `--fly`) and `uml/model.puml` (group → users association).
- Audit technical restrictions and the roadmap commits `[F2-C01]`…`[F2-C10]`.
- Commit with the mandatory message `[F2-C10] Complete delivery 2 integration`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `execution-control`: `HELP` shows the exact Delivery 2 syntax.
- `park-management`: in-memory relationships are object references with the prescribed directions.
- `project-build`: Delivery 2 technical restrictions and commit roadmap.

## Impact

- `Group`, `GroupService`, `GroupFileStorage`, `ParkController`, `CommandProcessor` (HELP text), tests, README, UML.
- Ambiguity flagged: the statement's placeholder spelling differs between phases (`<firstName>` in Delivery 1, `<FirstName>` in Delivery 2); Delivery 2 spelling is used as it is the latest "at a minimum" list.
