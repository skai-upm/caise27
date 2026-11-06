# Design

## Context

`PassRepository` (`passes.txt`, with the consumed flag and price) and the attraction type field already exist. `TextParkStorage` and `AppConfiguration` use the same classes in normal and `--fly` mode. Cascades are implemented in `ParkController.deleteTicket`/`deleteGroupTicket`, followed by `save()`. The UML diagram has passes linked to tickets but without per-kind cardinalities.

## Goals / Non-Goals

**Goals:** evidence through restarts and `--fly`, and a diagram that states the Delivery 3 elements explicitly.
**Non-Goals:** user persistence, format changes.

## Decisions

- **`Delivery3PersistenceTest`** uses `App.run` with a temporary base directory and the configuration customizer to register users. Run 1 creates typed attractions, tickets, a group ticket, passes, a SPEED entry (consumption), a refund and a cascade deletion. Run 2 inspects the restored state through commands and `ParkController` queries. A third run uses `--fly`, and the test asserts that `tmp/` is empty and `data/` is byte-identical to before.
- **UML**: replace the single `Pass --> IndividualTicket` with `IndividualTicket "1" <-- "0..*" SpeedPass` and `IndividualTicket "1" <-- "0..1" SpecialPass`, which keeps the direction (passes know their ticket). `UmlModelConsistencyTest` gains assertions for the listed Delivery 3 elements.

## Risks / Trade-offs

- [A verification-only change may find no gaps] → The tests stay as regression guards. Any gap found is fixed within this change.
