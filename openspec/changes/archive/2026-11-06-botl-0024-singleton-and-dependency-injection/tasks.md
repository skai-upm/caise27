# Tasks

## 1. Repository configuration

- [x] 1.1 Add `ParkRepositories` (in-memory and file-loaded factories) and verify with a unit test that loading two directories yields distinct repository instances with their own data

## 2. Singleton controllers and injection

- [x] 2.1 Turn the five services into singletons with `configure(...)`, make `ParkController.start(ParkRepositories, LocalDate)` inject repositories and wire collaborations, keep the in-memory convenience `start(UserRepository, LocalDate)`, and adapt tests; verify the full suite passes
- [x] 2.2 Make `Main` build the repository configuration from the data directory and start the park with it; verify `MainTest` (normal and `--fly`) still passes
- [x] 2.3 Add tests for singleton identity and private constructors of every controller, and for switching between two repository configurations without code changes

## 3. Integration

- [x] 3.1 Add `<<singleton>>` stereotypes to `uml/model.puml`; run `mvn clean package` and verify all tests pass; compare JAR output for the Delivery 2 file (normal and `--fly`) with the previous build
- [x] 3.2 Run `openspec validate botl-0024-singleton-and-dependency-injection --strict` and verify the change is valid
