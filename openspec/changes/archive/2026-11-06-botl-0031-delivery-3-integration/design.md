# Design

## Context

See proposal.md – Why. All Delivery 3 features are implemented (F3-C01..F3-C09) with guard tests: `ArchitectureTest` (layers), `UmlModelTest`, `DependencyConfigurationTest`, `PasswordProtectionTest`, `AttractionTypesTest`, `PassConsumptionTest`, and end-to-end `MainTest` cases for Deliveries 1 and 2. `CommandCatalog` currently shows `ATTRACTION CREATE …[;<type>]` and `ATTRACTION ENTER …[;SPEED]`.

## Goals / Non-Goals

**Goals:**
- Literal coverage of the Delivery 3 HELP list; one end-to-end Delivery 3 run in both persistence modes.

**Non-Goals:**
- Behavior changes.

## Decisions

- **Catalog**: replace the two bracketed entries with four entries (`ATTRACTION CREATE;<id>;<name>;<capacity>` creating a GENERAL attraction, `…;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `…;SPEED`), each with its own purpose; the `<type>` note moves to the typed variant. Commands' parsing is unchanged (field ranges already accept both forms).
- **End-to-end test** in `MainTest` with a Delivery 3 file (four typed attractions, group of three, passes on direct and constituent tickets, SPEED individual and group entries, special entry, failed atomic entry, refunds, invoices, `HELP EXTENDED`, `EXIT`), comparing `--fly` and normal outputs and checking expected lines and that `tmp/` is empty.
- **README**: list the separate variants and a short Delivery 3 design summary.
