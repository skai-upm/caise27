# Tasks

## 1. Robustness

- [x] 1.1 Wrap command dispatch in `CommandProcessor.process` with a catch-all for unexpected runtime errors and verify with `CommandProcessorTest` (a service throwing `IllegalStateException` → `ERROR` printed, next command still processed)

## 2. Integration verification

- [x] 2.1 Add `Delivery1IntegrationTest` running the section 16 example in file and interactive modes (same output and stored data) plus an invalid-input sequence that keeps the application running, and verify it passes with `mvn package`
- [x] 2.2 Verify the technical restrictions: `mvn dependency:list` shows no compile/runtime dependencies and production sources contain no regex, JDBC, serialization or encryption usage
- [x] 2.3 Clone the repository into an empty scratch directory, run `mvn package` there and verify `java -jar target/amusement-park.jar input.txt` runs the example and interactive mode works
