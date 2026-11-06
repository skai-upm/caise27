# Tasks

## 1. Model

- [x] 1.1 Add `Pass`, `SpeedPass` and `SpecialPass` (pricing, consume, refundable check), and verify with `PassTest` the 20%/100% prices (including 6.40 for a 32.00 ticket) and the refundable rule

## 2. Controller

- [x] 2.1 Add pass storage and `buySpeedPasses`/`buySpecialPass`/`refund` with all rejections, and verify with `ParkControllerPassTest` the 1/10 packages, atomic conflicts, the one-Special-Pass rule (also after consumption), refunds and their rejections
- [x] 2.2 Cascade pass deletion in `deleteTicket` and `deleteGroupTicket` (validate first, then remove), and verify with tests that passes from several purchases are removed and that a rejected deletion keeps everything

## 3. Persistence and invoices

- [x] 3.1 Add `PassRepository`, `StoragePaths.passes()`, passes in `ParkState`/`TextParkStorage`/`AppConfiguration`, and verify with a round-trip test that prices, tickets (including composed) and the consumed flag survive
- [x] 3.2 Add the pass sections to `UserInvoiceBuilder`, supplied by `InvoiceController`, and verify the 88.00 example and that group invoices exclude passes

## 4. Commands and documentation

- [x] 4.1 Add the four handlers and catalog entries, and verify with `PassCommandsTest` the outputs, the errors and the HELP lines
- [x] 4.2 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
