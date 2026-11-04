# Design

## Context

Production sources: 19 classes in `com.parque` (see proposal). The current `uml/model.puml` was hand-extended by changes 5–8 and contains methods, `{readOnly}`/`{static}` labels, dependency arrows for most classes and labels that differ from field names (`owner` for `IndividualTicket.user`, `buyer` for `GroupTicket.group`). `UmlModelTest` only checks that each class is declared and a few relationship strings.

## Goals / Non-Goals

**Goals:** a diagram that is mechanically verifiable against the code; notation as prescribed by the source.

**Non-Goals:** changing any production class; showing methods; documenting future classes.

## Decisions

- **Relationship kinds**:
  - Composition (`*-->`): `GroupTicket.constituents` (constituent tickets are created by and deleted with their group ticket).
  - Aggregation (`o-->`): collections of independent objects — `Group.members`, `Attraction.present`, and the services' in-memory collections (`UserService.users`, `GroupService.groups`, `TicketService.tickets/groupTickets`, `AttractionService.attractions`), plus `GroupTicket.group` (the group exists independently of the ticket).
  - Association (`x-->`): single references to collaborators (`CommandProcessor.users/groups/tickets/attractions`, services' `repository`, `GroupService.users`, `TicketService.users/groups/groupRepository`, `IndividualTicket.user`).
- **Multiplicities**: target end from the field type (single reference → `1`, or `0..1` when the code allows `null` — `IndividualTicket.user`, `GroupTicket.group`; list → `0..*`, `1..*` for constituents since an empty group cannot buy). Holder end only where inferable (`0..*` for many tickets per user/group, `1` for service-owned collections).
- **Usage arrows** (`..>`, labelled `«use»`): only for classes with no incoming relationship: `App ..> CommandProcessor` and `App` creating services/repositories is not drawn because those already have incoming associations; `CommandProcessor` (created by `App`), `UserValidator` (used by `UserService`, `GroupService`, `TicketService`, `AttractionService`), `UserField` (used by `User`, `UserService`, `CommandProcessor`). `App` itself is the entry point: nothing uses it, so its own usage of `CommandProcessor` is what is shown.
- **Verification test**: `UmlModelTest` parses relationship lines with a regular expression (test code only), loads the holder class by name via reflection and asserts it declares a field named like the label whose type (or generic element type) is the target class; it also asserts no `(` inside class bodies and no `{readOnly}`/`{static}` labels, and runs on every build so later changes keep the model honest.

## Risks / Trade-offs

- [Cross and diamond cannot be combined on one end in PlantUML] → aggregations/compositions mark the holder with the diamond, which already identifies it unambiguously.
