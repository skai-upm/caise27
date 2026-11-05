# Tasks

## 1. Maven project generation

- [x] 1.1 Generate the project at the repository root with `maven-archetype-quickstart` (groupId `es.upm.parque`, artifactId `amusement-park`) and verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `pom.xml` with `maven.compiler.release` = 21 and verify via `mvn help:effective-pom` (or pom inspection) that release 21 is applied

## 2. Build verification

- [x] 2.1 Run `mvn -q verify` and verify it ends with BUILD SUCCESS including the archetype test
- [x] 2.2 Verify compiled `App.class` has class-file major version 65 (Java 21) with `javap -v`
- [x] 2.3 Ensure `target/` stays git-ignored and only sources and `pom.xml` are versioned (verify with `git status`)
