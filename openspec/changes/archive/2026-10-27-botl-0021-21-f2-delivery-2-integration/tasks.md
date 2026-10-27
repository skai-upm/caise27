# Tasks

## 1. HELP syntax

- [x] 1.1 Update the HELP user command lines and USER syntax error messages to the Delivery 2 placeholders and adjust existing tests; verify `CommandProcessorTest` checks the full Delivery 2 HELP list from the source

## 2. Integration and restrictions

- [x] 2.1 Add `Delivery2IntegrationTest` (complete session with expected outputs and rejections, normal and `--fly`, persistence and cleanup) and `ModelStructureTest` (relationship directions, ticket hierarchy); verify with `mvn package`
- [x] 2.2 Clone the repository into a scratch directory, build with `mvn package`, and run the four execution forms with an `input_fase2.txt`; verify the mandatory Delivery 2 commits are present in order with `git log`
