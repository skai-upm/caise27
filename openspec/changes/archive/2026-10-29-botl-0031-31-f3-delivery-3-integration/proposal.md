# Proposal

## Why

Milestone F3-C10 closes Delivery 3: commands from all three deliveries must coexist, `HELP` and `HELP EXTENDED` must cover them, and the Delivery 3 technical restrictions and commit roadmap must hold, with the JAR runnable from a command file both normally and with `--fly`.

## What Changes

- End-to-end Delivery 3 integration test running a combined session (users with BCrypt, groups, tickets, group tickets, typed attractions, SpeedPass/Special Pass purchase, refund and consumption, invoices, HELP/HELP EXTENDED) from a file, restarting on the same data, and in `--fly` mode (no data left behind); the Delivery 1 and 2 sessions keep passing.
- Verify `HELP` contains every Delivery 3 syntax listed by the statement (plus Deliveries 1–2) and `HELP EXTENDED` describes each.
- Update `README.md` for Delivery 3 (new commands, attraction types, passes, invoices, BCrypt, data files, `--fly`).
- Audit Delivery 3 restrictions (layered packages, Builder, Singleton controllers, injected non-singleton repositories, BCrypt only external runtime dependency, bounded generics, consumed passes kept, atomic group SpeedPass entry) and the `[F3-C01]`…`[F3-C10]` roadmap.
- Commit with the mandatory message `[F3-C10] Complete delivery 3 integration`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `project-build`: Delivery 3 restrictions and commit roadmap.
- `execution-control`: help covers all three deliveries.

## Impact

- New `DeliveryThreeIntegrationTest`, `README.md`; no production behavior change expected.
- Ambiguity flagged: the statement's example file `input_fase3.txt` is supplied by instructors; the test uses an equivalent combined session.
