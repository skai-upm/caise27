# Tasks

## 1. Cascading, atomic deletions

- [x] 1.1 Add `IndividualTicket.clearPasses()` and make `TicketController.delete`/`deleteGroupTicket` delete passes, then internal tickets, then the group ticket after all checks; verify `TicketDeletionCascadeTest` (passes gone after TICKET DELETE and GROUPTICKET DELETE, also after restart; rejected deletions keep everything)
- [x] 1.2 Make `TicketRepository.save` replace its three files together (two-phase write via `TextFiles.writeAll`) and verify atomicity under storage failure: with `tickets.txt` read-only, TICKET DELETE and GROUPTICKET DELETE report an error and tickets, internal tickets and passes remain in memory and on disk

## 2. Persistence coverage

- [x] 2.1 Add restart tests for consumed passes on internal tickets and for attraction types of all four kinds, and a `--fly` test where passes are bought and consumed without touching `data/passes.txt`; verify they pass

## 3. Model and end-to-end

- [x] 3.1 Review and update `uml/model.puml` for the Delivery 3 design; verify PlantUML `-checkonly`, the UML cross-check script and a rendered image
- [x] 3.2 Run `mvn package`, the regression script (no output change expected) and an end-to-end JAR scenario with cascading deletions and restart; verify outputs and data files
