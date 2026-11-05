# Tasks

## 1. Load hardening

- [x] 1.1 Expose `UserFileStore.getMalformedLineCount()` and make `UserRepository(UserFileStore)` skip invalid users and duplicate DNI/email while loading, exposing `getIgnoredOnLoad()` (malformed + rejected); verify `UserRepositoryTest` for invalid DNI line, duplicate DNI line, clean file (0 ignored)
- [x] 1.2 In `App.main`, print `WARNING: <n> invalid line(s) in users.txt were ignored` when `getIgnoredOnLoad() > 0`, and catch `UncheckedIOException` from repository construction to print `ERROR: cannot read users file 'users.txt'` and return; verify with an end-to-end JAR run where `users.txt` is a directory (ERROR, no stack trace) and one with an invalid line (warning)

## 2. Integration and restriction guards

- [x] 2.1 Add `Delivery1IntegrationTest` running the §16 sequence through `App.start` in file mode and interactive mode with a file-backed store in a temp dir; verify all responses OK in order, Ana's phone and email updated, Pedro deleted, persisted file content, and identical output for both modes
- [x] 2.2 Add `TechnicalRestrictionsTest` asserting every pom dependency has `test` or `import` scope and that `src/main/java` contains no `java.util.regex`, `.matches(`, `replaceAll(`, `replaceFirst(`; verify `mvn test` passes

## 3. Documentation and full verification

- [x] 3.1 Write `README.md` with requirements (JDK 21+, Maven), build (`mvn package`), interactive and file execution, the `users.txt` persistence file and the command list; verify the documented commands run as written
- [x] 3.2 From a clean copy of the project sources (pom.xml, .mvn, src) in a temp dir, run `mvn package` and the §16 file through `java -jar target/amusement-park.jar input.txt` and interactively via stdin; verify BUILD SUCCESS and expected output
- [x] 3.3 Resolve Sonar finding java:S2095 in `App.main` (console PrintStream not closed) by holding the console in a never-closed static final field (closing standard output broke the test JVM, so try-with-resources is not applicable); verify `mvn package` tests and end-to-end runs still pass and the SDDX apply quality gate is OK
