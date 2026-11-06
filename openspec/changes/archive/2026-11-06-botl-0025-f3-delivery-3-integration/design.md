# Design

## Context

All Delivery 3 features are implemented and unit-tested: layered packages, exceptions, Builder invoices, singleton controllers with injection, help strategies, BCrypt, passes, typed generic attractions, SPEED and consumption, and persistence plus UML. `App.run` accepts a configuration customizer that tests use to register users, because USER commands come later in the batch.

## Goals / Non-Goals

**Goals:** one realistic Delivery 3 session that guards integration, and recorded evidence for each technical restriction.
**Non-Goals:** USER commands, and replacing email references with user objects (open item for the user-modification milestone, where email changes make it necessary).

## Decisions

- **`Delivery3IntegrationTest`** follows the Delivery 2 integration test style: one `input_fase3.txt` session through `App.run`, asserting outputs, then a restart run that checks persisted results, then the same file with `--fly`, asserting no residue and unchanged `data/`.
- **HELP verification**: the Delivery 3 lines are added to the verbatim list in `CommandProcessorTest`. `HelpCommandsTest` already checks that HELP EXTENDED has a purpose for each catalog entry.
- **Restriction review** with commands: the dependency list (jBCrypt only), a grep for forbidden APIs, the `LayeredArchitectureTest`, `getInstance` only on controllers, no static state in repositories, and the bounded generics in `Attraction`.

## Risks / Trade-offs

- [The instructors' `input_fase3.txt` will include USER CREATE lines that are rejected until the user milestones are processed] → Documented in the proposal. Those milestones follow in this batch.
