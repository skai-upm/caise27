# Tasks

## 1. Model and service

- [x] 1.1 Add `getIndividualTickets()` to the access capability, `acceptsSpeedPasses()` and `consumeOnEntry` to attractions (Special Pass consumption in `SpecialAttraction`), `EntryResult`, and `AttractionService.enter(attraction, ticket, speed)` with all checks before any change; verify with `AttractionServiceTest` every scenario of the spec (individual/group SPEED, indivisibility, special consumption, rejections leave passes unchanged)

## 2. Command

- [x] 2.1 Accept the optional `SPEED` element in `ATTRACTION ENTER`, print consumed passes, add the variant to `CommandCatalog`, update `uml/model.puml`; verify with `CommandProcessorTest`, `HelpGeneratorTest`, `UmlModelTest`, PlantUML check and `mvn package`
