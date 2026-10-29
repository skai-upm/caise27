# Tasks

## 1. Model and operations

- [x] 1.1 Add `Pass`, `SpeedPass`, `SpecialPass`; verify with unit tests for prices (8.00 / 40.00 / 6.40 / rounding), consumed state and refundability boundaries
- [x] 1.2 Add pass registry, `findIndividualTicket`, `buySpeedPasses`, `buySpecialPass`, refunds and cascade removal in `deleteTicket`/`deleteGroupTicket`; verify with unit tests for every scenario of the `passes` spec (package of ten, single naming, rejections creating nothing, second Special Pass, refunds allowed/denied, cascades)

## 2. Commands and persistence

- [x] 2.1 Dispatch the four pass commands, add catalog entries with descriptions; verify with processor tests and HELP output
- [x] 2.2 Add `PassRepository`, `TextFilePassRepository`, extend `Repositories` and the factory, reconstruct passes on load; verify with a restart test preserving ticket, price and consumed state
- [x] 2.3 Update `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run of the pass commands
