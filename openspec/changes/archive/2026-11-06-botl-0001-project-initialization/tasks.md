# Tasks

## 1. Maven project scaffold

- [x] 1.1 Generate the project from `maven-archetype-quickstart` (groupId `es.upm.park`, artifactId `amusement-park`) at the repository root and verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `pom.xml` with `maven.compiler.release` 21 and UTF-8 source encoding, and verify with `mvn help:effective-pom` (or by inspecting the pom) that the release is 21
- [x] 1.3 Ensure `target/` stays ignored by Git and verify `git status` shows no build output

## 2. Verification

- [x] 2.1 Run `mvn -q compile` and verify BUILD SUCCESS with classes produced under `target/classes`
- [x] 2.2 Run `mvn test` and verify the archetype placeholder test passes
- [x] 2.3 Document build prerequisites (JDK 21, Maven) and the build command in `README.md` and verify the documented command runs as written
