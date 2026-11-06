# Tasks

## 1. Help

- [x] 1.1 Split the attraction create/enter catalog entries into the four Delivery 3 syntaxes with purposes, update README and help assertions, and verify every syntax of the Delivery 3 list appears in `HELP` and `HELP EXTENDED`

## 2. End-to-end verification

- [x] 2.1 Add a `MainTest` running a Delivery 3 command file with and without `--fly` (identical output, expected results, no leftover temporary data) and verify the Delivery 1/2 end-to-end tests still pass
- [x] 2.2 Verify the technical restrictions: guard tests pass and the only runtime dependency is `org.mindrot:jbcrypt`

## 3. Integration

- [x] 3.1 Run `mvn clean package` and verify all tests pass; run the JAR in an empty directory with the Delivery 3 file normally and with `--fly`
- [x] 3.2 Run `openspec validate botl-0031-delivery-3-integration --strict` and verify the change is valid
