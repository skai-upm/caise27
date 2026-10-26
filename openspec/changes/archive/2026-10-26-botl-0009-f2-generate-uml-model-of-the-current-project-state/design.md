# Design

## Context

Documentation-only change (see proposal.md). The source of truth is the field inventory of `src/main/java/es/upm/parque`.

## Goals / Non-Goals

**Goals:**
- A diagram derivable mechanically from fields: value-typed fields → attributes; fields typed with a project class (directly or as a collection) → relationships.

**Non-Goals:**
- Any refactoring to make the code "look better" in the diagram.

## Decisions

- **Attributes**: instance fields of type `String`, `int`, `boolean`, `LocalDate`, `BigDecimal`, `Path` (value data, not project classes) as `name : Type`. Static constants and the console `PrintStream` of `CommandRunner` are omitted as technical, non-domain details.
- **Relationships**: one per reference field, label = field name, target multiplicity `1` for a single reference and `0..*` for collections (`1..*` for `GroupTicket.tickets`, which can never be empty). Source multiplicity is only shown where the code fixes it (`GroupTicket` `1` for its composed tickets). Plain references use `x-->` (cross at the holder). `GroupTicket.group` is drawn as aggregation (`o-->`) and `GroupTicket.tickets` as composition (`*-->`), as required by the group-ticket epic; PlantUML cannot draw a cross and a diamond on the same end.
- **Usage**: classes without incoming relationships — `CommandRunner` (used by `App`), `UserValidator` and `UserField` (used by `UserManager`), `UserException` (used by the managers and `Attraction`) — get `..>` dependencies. `App`, the entry point, has no user.
- **Validation**: PlantUML 1.2024.7 `-checkonly`, plus a review of each element against the field inventory.

## Risks / Trade-offs

- [Interpreting "only primitives and enumerations" strictly would drop dates, prices and paths] → they are shown because they are value data, not relationships to modeled classes.
