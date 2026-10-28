# Tasks

## 1. Maven project scaffolding

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.etsisi.amusementpark`, artifactId `amusement-park`) into the repository root and verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `maven.compiler.release` to 21 in `pom.xml` and verify with `javap -v` on a compiled class that the major version is 65

## 2. Build verification

- [x] 2.1 Run `mvn package` and verify `BUILD SUCCESS`, passing tests and a JAR in `target/`
- [x] 2.2 Commit `pom.xml` and the source structure with the exact message `[F0-C01] Initialize Maven project with JDK 21` and verify with `git log --format=%s`
