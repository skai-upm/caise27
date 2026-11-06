# Tasks

## 1. HELP alignment

- [x] 1.1 Replace `HELP_TEXT` and the affected usage strings with the exact Delivery 2 syntax, and verify with an updated `CommandProcessorTest` that every mandated line appears verbatim and that the existing HELP tests pass with the new spelling

## 2. Integration

- [x] 2.1 Add `Delivery2IntegrationTest` (full session file, normal run plus restart, `--fly` run with no residue), and verify that it passes
- [x] 2.2 Review the Delivery 2 technical restrictions with `mvn dependency:list`, a grep of `src/main` for regex/serialization APIs, and a check of the singleton and the shared repositories, and verify that none is violated (documenting the user-reference open item)

## 3. Delivery

- [x] 3.1 Update `README.md` with a Delivery 2 example session, and verify that `mvn clean package` passes and that `java -jar target/amusement-park.jar --fly <file>` runs the example and leaves no data
