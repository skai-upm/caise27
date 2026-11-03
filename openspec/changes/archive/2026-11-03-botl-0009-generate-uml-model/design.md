# Design

## Context

The current `uml/model.puml` was extended in each previous change and includes methods, `{readOnly}` labels and many usage arrows. The source of this change defines precise notation rules. See proposal.md.

## Goals / Non-Goals

**Goals:** a diagram that mirrors the fields actually declared in `src/main/java`, with the required notation.

**Non-Goals:** any code change, refactoring or new element; diagrams other than the class diagram.

## Decisions

- **Methods vs "relevant public methods" (explicit conflict resolution):** the general list asks for "relevant public methods", but the acceptance criteria state "Do not represent usage relationships or methods in classes". The acceptance-criteria rule is the more specific notation instruction and is applied: no methods are drawn. "Relevant public methods" is therefore intentionally not rendered; this is recorded here so it can be revisited.
- **Attributes:** only instance fields with primitive, `String`, enumeration or JDK value types (`LocalDate`, `BigDecimal`, `Path`) are listed, as `name : Type` with visibility. Constants (`static final`) and labels such as `{readOnly}` are technical details and are omitted. Fields typed with model classes (or collections of them) are not listed; they become relationships.
- **Relationships:** for each model-typed field, one line from the holder to the target, labelled with the field name, with target multiplicity (`1`, `0..1` where the field may be null, `*` for lists, `1..*` for the non-empty composition). Plain associations use `x-->` (cross at the holder, arrow at the target). Aggregation (`o-->`) is used where the holder keeps references to independently existing objects in a collection or the group ticket's `group`; composition (`*-->`) only for `GroupTicket.tickets`, whose tickets are created by and belong to the group ticket. No inverse lines.
- **Inheritance:** `ParkTicket <|-- Ticket` and `ParkTicket <|-- GroupTicket`; `ParkTicket` drawn as abstract.
- **Usage arrows** only for classes with no incoming relationship: `CommandProcessor` (created by `App`), `UserValidator` (used by the services), `UserField` (used by `User`, `UserService`, `CommandProcessor`). `App` has no incoming relationship and no user in the code; it is marked with a note as the entry point.
- Validation with the PlantUML CLI (`-checkonly`); `mvn clean test` re-run to show behaviour is unchanged.

## Risks / Trade-offs

- [Interpreting "technical labels"] → taken as UML property strings/stereotypes and constants; types are kept because they are needed to read attributes.
