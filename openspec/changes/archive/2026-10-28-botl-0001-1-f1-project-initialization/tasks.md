# Tasks

## 1. Maven project scaffolding

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm`, artifactId `amusement-park`, package `es.upm.amusementpark`) and place `pom.xml`, `src/main/java` and `src/test/java` in the repository root; verify the files exist at the root
- [x] 1.2 Set `maven.compiler.release` to 21 (and UTF-8 source encoding) in `pom.xml`; verify the property is present in `pom.xml`
- [x] 1.3 Run `mvn package` and verify it ends with `BUILD SUCCESS` with all tests passing

## 2. Milestone commit

- [x] 2.1 Commit `pom.xml` and the project structure (no build output) with the exact message `[F0-C01] Initialize Maven project with JDK 21`; verify with `git log -1 --format=%s`
