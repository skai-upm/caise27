# Proposal: F2 — Amusement Park, Delivery 2

## Why

Delivery 1 manages only persistent users. Delivery 2 turns the application into a stateful park: a single central manager with a configurable system date and base price, user groups, individual and discounted group tickets, attractions with capacity-controlled admission, persistence of the new elements, an isolated `--fly` execution mode, and a PlantUML model faithful to the code. All Delivery-1 behaviour must keep working (milestones F2-C01 to F2-C10).

## What Changes

- Single central park manager owning users, groups, tickets and attractions; `SYSTEM DATE;<yyyy-MM-dd>` (initially the local start date) and `SYSTEM PRICE;<Price>` (initially 40.00 €, affects only later purchases, not persisted) (F2-C01).
- `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, with unique members and a lock derived from active group tickets (F2-C02).
- Common ticket abstraction and shared identifier space; `TICKET CREATE` (non-past date, current price, shows price) and `TICKET DELETE` (strictly future, standalone only) (F2-C03).
- `GROUPTICKET CREATE` composing one individual ticket `<id>:<dni>` per member with bracket discounts, atomic on identifier conflicts; `GROUPTICKET DELETE` (strictly future, deletes composition) (F2-C04).
- **Modified Delivery-1 behaviour:** `USER DELETE` is now rejected while the user has an active ticket or belongs to a locked group; otherwise the user is also removed from their groups (F2-C03/C04).
- `ATTRACTION CREATE` and read-only `ATTRACTION LIST` (F2-C05).
- `ATTRACTION ENTER` with individual tickets valid today and capacity checks (F2-C06), extended to all-or-nothing group admission; `ATTRACTION EXIT` and `ATTRACTION STATUS` (F2-C07).
- Text-file persistence of groups, tickets (with references and historical holder data) and attractions, without occupants; `--fly` mode using the same repositories in a timestamped temporary directory deleted at the end (F2-C08).
- `uml/model.puml` class model (F2-C09).
- **Modified Delivery-1 behaviour:** `HELP` lists every Delivery 1 and 2 command; integration of both deliveries (F2-C10).

## Capabilities

### New Capabilities

- `park-control-and-system-settings`: single central park manager, system date and base price.
- `group-management`: groups of registered users and their ticket-derived lock.
- `individual-ticket-management`: ticket abstraction, shared identifier space, individual ticket lifecycle.
- `group-ticket-purchasing`: group ticket composition, discounts and lifecycle.
- `attraction-catalog-and-occupancy`: attraction creation and occupant listing.
- `individual-attraction-entry`: admission with an individual ticket.
- `atomic-group-entry-and-exit`: group admission, exits and attraction status.
- `park-persistence-and-fly-mode`: persistence of the Delivery-2 elements and the `--fly` mode.
- `plantuml-model`: `uml/model.puml` documenting the implemented model.
- `delivery-two-integration`: coexistence of all Delivery 1 and 2 commands under the technical restrictions.

### Modified Capabilities

- `user-deletion`: deletion is additionally constrained by active tickets and locked groups, and removes the user from groups.
- `help-and-exit`: `HELP` must list the Delivery-2 commands too.

## Impact

- Production code under `src/main/java/es/upm/etsisi/amusementpark/`: new park manager, group, ticket hierarchy, attraction and repository classes; `UserService` is folded into the park manager; `App` gains `--fly` argument handling.
- New data files next to `users.txt`: `groups.txt`, `tickets.txt`, `attractions.txt`; `--fly` uses `tmp/<yyyyMMdd_HHmmss>/`.
- New `uml/model.puml`.
- Still Java standard library only; no serialization, databases or frameworks.
