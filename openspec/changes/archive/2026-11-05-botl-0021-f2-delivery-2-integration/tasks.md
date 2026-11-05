# Tasks

## 1. HELP alignment

- [x] 1.1 Replace `HELP_TEXT` with the §15 syntax lines in order (plus the field list line) and update every test asserting old placeholder spellings; verify a test that the help output contains all §15 lines in order

## 2. Integration verification

- [x] 2.1 Add `Delivery2IntegrationTest` (normal run, restart restoring state with empty attractions, `--fly` run with identical responses and no data left); verify `mvn test`
- [x] 2.2 Add `RelationshipDirectionTest` (no User→ticket/group/attraction fields, no Group→ticket fields, ticket inheritance and composition); verify `mvn test`

## 3. Documentation and submission check

- [x] 3.1 Update `README.md` with Delivery 2 commands, data files and `--fly`; verify documented commands
- [x] 3.2 From a clean copy of the sources run `mvn package`, then `java -jar target/amusement-park.jar input_fase2.txt` and `--fly input_fase2.txt`; verify BUILD SUCCESS, expected output and no leftover fly data
