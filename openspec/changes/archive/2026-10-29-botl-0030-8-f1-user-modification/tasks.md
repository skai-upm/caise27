# Tasks

## 1. User update

- [x] 1.1 Add `UserField` and `ParkController.updateUser` (password check first, creation rules per field, email re-keying) and the `USER UPDATE` dispatch; verify with tests for the four source examples, DNI/unknown field, wrong password, invalid/duplicate email, weak password, bad card, and email change keeping tickets and invoices
- [x] 1.2 Verify persistence through `App.run` (phone change survives restart; email change reloads under the new email with its ticket); update `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run of the Delivery 1 example flow
