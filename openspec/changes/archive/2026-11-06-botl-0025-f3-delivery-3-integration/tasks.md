# Tasks

## 1. HELP

- [x] 1.1 Add the Delivery 3 syntax lines to the verbatim HELP check in `CommandProcessorTest`, and verify that HELP and HELP EXTENDED tests pass

## 2. Integration

- [x] 2.1 Add `Delivery3IntegrationTest` (full session, restart checks, `--fly` without residue), and verify that it passes, fixing any defect it reveals
- [x] 2.2 Review the Delivery 3 technical restrictions (dependency list, forbidden APIs, layers, singletons vs repositories, injection, generics, consumed passes kept, atomic group SPEED), and verify each with the command or test that shows it
- [x] 2.3 Build with `mvn clean package` and run `java -jar target/amusement-park.jar input_fase3.txt` and `--fly input_fase3.txt` from a scratch directory, and verify both runs and that no `tmp/` data remains

## 3. Documentation

- [x] 3.1 Add a Delivery 3 example session to `README.md`, and verify that the commands it shows are accepted by HELP
