# Design

## Context

All Delivery 2 features exist (park management, groups, individual and group tickets, attractions with access/exit/status, persistence with `--fly`, UML). HELP text and usage messages were written incrementally with slightly different placeholder names. User commands do not exist yet, because their Delivery 1 milestones come later in this batch.

## Goals / Non-Goals

**Goals:** exact Delivery 2 HELP, one realistic end-to-end session that guards integration, and an explicit restriction review.
**Non-Goals:** implementing users or changing domain relationships for users (user-model milestone).

## Decisions

- **HELP text = the mandated list, verbatim and in the source order**, one syntax per line, after `Available commands:`. The usage hints in error messages reuse the same strings so that HELP and errors never disagree.
- **`Delivery2IntegrationTest`** drives `App.run` with a temporary base directory and a `TestUsers` directory, because users cannot be registered by command yet. It runs one command file in normal mode, restarts to check that groups, tickets and attractions persist and that attractions restart empty, then runs the same file with `--fly` and asserts that `tmp/` is empty afterwards.
- **Restriction review** as a task checklist backed by commands: `mvn dependency:list` (test scope only), a grep for `java.util.regex`, `Serializable`, `ObjectOutputStream` and `matches(`, a single `getInstance` with a private constructor, and the same `TextParkStorage` used for both modes.

## Risks / Trade-offs

- [Users referenced by email conflict with "relationships as objects"] → Cannot be resolved without the user model. Documented as an open item for the user-model milestone, which should replace email references with user object references.

## Open Questions

- None that block this change. The user-reference gap above is deferred to the user-model milestone.
