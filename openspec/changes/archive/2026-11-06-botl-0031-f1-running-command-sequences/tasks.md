# Tasks

## 1. Verification

- [x] 1.1 Add `CommandSequencesTest` with the milestone example (USER CREATE ×2, USER UPDATE, EXIT, blank line), and verify that stdin and file runs produce identical output in file order and that a nonexistent file gives a controlled error
- [x] 1.2 Build with `mvn clean package` and run the example with `java -jar target/amusement-park.jar --fly input.txt` and through piped stdin, and verify both outputs
