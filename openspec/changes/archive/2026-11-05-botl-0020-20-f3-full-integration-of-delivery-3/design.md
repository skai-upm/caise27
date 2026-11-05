# Design

## Context

Final architecture: presentation (`App`, `CommandRunner`, `CommandProcessor`, help classes) → business (`ApplicationContext`, singleton `ParkController` and managers) → persistence (repositories) → model. Existing tests cover each feature; no test exercises all deliveries in one run. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** one end-to-end scenario per mode; help/dispatcher consistency; release check.

**Non-Goals:** new features; refactoring passing code.

## Decisions

- **Integration resource** `src/test/resources/integration/input_fase3.txt`: ~40 valid commands (users incl. password update, group with three members, individual and group tickets, SYSTEM PRICE, SpeedPass pack and Special Pass, GENERAL/GROUP/SPECIAL attractions with normal, SPEED and SPECIAL entries, STATUS, EXIT from attraction, refund, invoices for user and group, cascade ticket deletion, HELP, HELP EXTENDED, EXIT).
- **`DeliveryIntegrationTest`** (presentation package, uses `App.run`): (a) file run on empty data — no `ERROR:` line, specific expected outputs, expected data files; (b) interactive run with the same commands — identical output; (c) `--fly` run over existing data — normal files byte-identical, no run directory left.
- **Help coverage test**: for every `CommandCatalog` entry, the keyword part of its syntax is sent with no parameters and must not yield `Unknown command`; both help outputs contain its syntax, extended help its description.
- **Release check**: `mvn clean package`, run the JAR with the resource file, PlantUML `-checkonly`, cross-check and layer scripts.
- **Defects**: any failure found is fixed minimally and recorded in the archived tasks.
