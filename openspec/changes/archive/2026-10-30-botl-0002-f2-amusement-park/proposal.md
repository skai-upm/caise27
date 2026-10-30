# Proposal

## Why

Delivery 1 manages only users. Delivery 2 of the Amusement Park assignment (milestones F2-C01 to F2-C10) turns the application into a park: a single central controller with a system date and base price, user groups, individual and group tickets with discounts, attractions with entry/exit, persistence of all these elements, a temporary `--fly` mode for isolated runs, and a PlantUML model of the design.

Source: `.botl/proposals/opus_5_setup_single_gpt/0002-2.F2-Amusement-Park/2.F2-Amusement-Park.md` (sections 3–16, 18, 20 and milestones 17.1–17.10).

## What Changes

- **F2-C01** Single central park controller holding users, groups, tickets, attractions, the current system date (initially today) and base price (initially 40.00 EUR). New commands `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.
- **F2-C02** `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`; a user appears at most once per group.
- **F2-C03** Common ticket abstraction; `TICKET CREATE` (current price, no past dates, price displayed) and `TICKET DELETE` (only dates strictly after today, not for tickets that belong to a group ticket). **BREAKING (behaviour)**: `USER DELETE` is now rejected while the user has an active ticket.
- **F2-C04** `GROUPTICKET CREATE` (non-empty group, composition of individual tickets `<id>:<dni>`, size-based discount, total displayed, atomic identifier check), `GROUPTICKET DELETE`, `GROUP DELETE`. Groups with an active group ticket are locked against `GROUP ADD`/`REMOVE`/`DELETE`; `USER DELETE` is rejected when the user is in a locked group and otherwise removes the user from their groups.
- **F2-C05** `ATTRACTION CREATE` and `ATTRACTION LIST`.
- **F2-C06/F2-C07** `ATTRACTION ENTER` with individual or group tickets (atomic, capacity-checked, ticket valid today), `ATTRACTION EXIT`, `ATTRACTION STATUS`.
- **F2-C08** Persist groups, tickets (including historical tickets of deleted users/groups) and attractions in text files; occupants are not persisted. `--fly` mode uses a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory, deleted at the end of the run.
- **F2-C09** `uml/model.puml` PlantUML class diagram consistent with the code.
- **F2-C10** Extended `HELP` with every Delivery 1 and 2 command; all commands coexist.

## Capabilities

### New Capabilities
- `park-state`: The single central park manager, current system date and current base ticket price.
- `user-groups`: Creating, populating, modifying and deleting groups of users, and the lock derived from active group tickets.
- `tickets`: Individual and group tickets, shared identifier space, pricing and discounts, and deletion rules.
- `attractions`: Attractions, entry with tickets, exit, listing and status.
- `park-persistence`: Storage and reconstruction of groups, tickets and attractions, and the temporary `--fly` mode.
- `design-model`: The PlantUML class model that documents the implemented design.

### Modified Capabilities
- `user-management`: `Delete user` now considers active tickets and group locks and removes the deleted user from groups.
- `command-interface`: `Command syntax` recognises the new commands; `HELP command` lists the Delivery 2 syntax.

## Impact

- New/changed code under `src/main/java/es/upm/etsisi/amusementpark/**` (model, repository, controller, cli, App) and tests.
- New runtime data files in the working directory: `groups.txt`, `tickets.txt`, `attractions.txt` (alongside `users.txt`); `tmp/` used by `--fly`.
- New file `uml/model.puml`.
- No new dependencies.
- Git milestone commits `[F2-C01] … [F2-C10]` are folded into one BOTL commit (listed in its body); flagged deviation from section 18 as in proposal 1.
