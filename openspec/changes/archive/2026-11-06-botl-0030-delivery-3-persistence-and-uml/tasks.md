# Tasks

## 1. Pass persistence

- [x] 1.1 Add `PassFileStore`, `Pass` restore constructors and `TicketRepository.findIndividual`, back `PassRepository` with the store in `ParkRepositories.load`, persist consumption after entry, add `passes.txt` to `.gitignore`; verify with store round-trip tests and restart tests for every new `park-persistence` scenario (including constituent tickets, consumed state, refunds, orphan records and `--fly`)

## 2. UML

- [x] 2.1 Update `uml/model.puml` with the Delivery 3 elements and extend `UmlModelTest` (inherited fields, source multiplicities, new packages); verify PlantUML `-checkonly` and the test pass

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR twice in a temp directory (buy/consume/refund passes, then restart and inspect) and once with `--fly`
- [x] 3.2 Run `openspec validate botl-0030-delivery-3-persistence-and-uml --strict` and verify the change is valid
