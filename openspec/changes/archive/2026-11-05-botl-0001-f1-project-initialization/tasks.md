# Tasks

## 1. Generate Maven project

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.amusementpark`, artifactId `amusement-park`) and move it to the repository root; verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `pom.xml` for Java release 21 and UTF-8 encoding; verify the properties are present in `pom.xml`

## 2. Verification

- [x] 2.1 Run `mvn -q test` at the repository root and verify the build succeeds with the archetype's sample test passing
- [x] 2.2 Verify `git status` does not list `target/` (ignored) and only the intended project files are new
