# Design

## Context

- 25 command operations are dispatched by `CommandProcessor` and registered in `CommandCatalog`.
- `App.launch(args, workDir, start, in, out, err)` drives normal, file and `--fly` runs through `ParkSession`.
- 53 production types are mirrored by `uml/model.puml` and checked by `UmlModelTest`.

See proposal.md.

## Goals / Non-Goals

**Goals:**
- Executable, repeatable evidence for every Delivery 3 acceptance criterion; zero speculative changes.

**Non-Goals:**
- New features or refactors without a demonstrated defect.

## Decisions

- **Scenario as a test resource:** the scenario file is used by both the JUnit test (copied into a temp dir) and the manual `java -jar` check, so automated and manual verification share one source.
- **Expected results:** the test asserts each reply's prefix (`OK`/`ERROR`) line by line against an expectation list. It also asserts the exact literals for purchases (`OK TICKET CREATED -> 50.00`, `OK GROUP TICKET CREATED -> 135.00`, `OK SPEEDPASS CREATED -> 100.00`, `OK SPECIALPASS CREATED -> 50.00`) and for pass consumption. The scenario sets `SYSTEM DATE` explicitly, so results do not depend on the real date.
- **Mode equivalence:** the scenario's printed output is the same in interactive, file and `--fly` runs, each in a fresh working directory.
- **Help coverage:** every `case` operation of the dispatcher (collected from `CommandCatalog` and the processor's acceptance of `<operation>;x`) appears in both help outputs.

## Verification matrix

| Acceptance criterion (F3-C10 / earlier milestones) | Evidence |
|---|---|
| `mvn package` generates the executable JAR | `mvn clean package`; manual `java -jar` scenario run (exit 0); `application-bootstrap` tests |
| E2 execution modes retained (interactive, file, `--fly`) | `Delivery3IntegrationTest` mode-equivalence test; `AppTest` |
| E1 and E2 commands available | scenario lines for USER/GROUP/TICKET/GROUPTICKET/ATTRACTION/SYSTEM; existing unit tests |
| New commands in HELP and HELP EXTENDED | `Delivery3IntegrationTest` help coverage; `CommandCatalogTest`, `HelpGeneratorTest` |
| Normal and temporary persistence with E3 | restart test (passes consumed, typed attractions, hashes) and `--fly` isolation test |
| UML corresponds to final code | `UmlModelTest` (class ↔ package parity, no methods/labels) + PlantUML `-syntax` |
| F3-C01 layers | `LayeringTest` |
| F3-C02 invoices / Builder | scenario invoice lines; `InvoiceBuilderTest` |
| F3-C03 singletons / DI | `ComponentWiringTest` |
| F3-C04 extended help | scenario `HELP EXTENDED`; `HelpGeneratorTest` |
| F3-C05 BCrypt | `password-protection` tests; restart test checks `$2a$` hashes |
| F3-C06 passes | scenario purchases/refund; `PassManagerTest` |
| F3-C07 attraction types | scenario GROUP/SPECIAL attractions; `AttractionTest` |
| F3-C08 pass-based entry | scenario SPEED/SPECIAL entries; `CommandProcessorTest` pass-entry cases |
| F3-C09 cascades / persistence | `CascadeDeletionTest`; scenario `TICKET DELETE` with passes |

## Risks / Trade-offs

- [Scenario brittleness when messages change] → Assertions use prefixes, except for literals fixed by the source requirements.
