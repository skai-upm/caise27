# project-initialization Specification

## Purpose
Defines the mandatory build baseline of the Amusement Park application: a Maven project generated from the quickstart archetype, targeting JDK 21, that compiles with Maven.

## Requirements

### Requirement: Standard Maven project structure
The project SHALL be a Maven project created from the `maven-archetype-quickstart` archetype, with a `pom.xml` at the repository root, production sources under `src/main/java`, and test sources under `src/test/java`. A different project structure MUST NOT be used.

#### Scenario: Clean checkout contains the Maven layout
- **WHEN** a developer inspects a clean checkout of the repository
- **THEN** `pom.xml` exists at the repository root
- **AND** the `src/main/java` and `src/test/java` source directories exist

#### Scenario: Non-Maven layout is rejected
- **WHEN** the repository lacks a root `pom.xml` or the standard source directories
- **THEN** the project does not satisfy this milestone and the build check fails

### Requirement: JDK 21 compilation target
The Maven build SHALL be configured to compile sources for Java release 21.

#### Scenario: Compiler release is 21
- **WHEN** the effective Maven configuration of the project is inspected
- **THEN** the Java compiler release is 21

#### Scenario: Language level is not lower than 21
- **WHEN** the project is compiled
- **THEN** the generated class files target Java 21 (class file major version 65)

### Requirement: Project compiles with Maven
The project SHALL compile and run its tests successfully using Maven on a JDK supporting release 21.

#### Scenario: Successful Maven build from clean checkout
- **WHEN** `mvn verify` is executed on a clean checkout with a JDK supporting release 21 and Maven available
- **THEN** compilation succeeds and the build finishes with `BUILD SUCCESS`

### Requirement: Standard library only
The application SHALL depend only on the Java standard library at runtime: the Maven project MUST NOT declare dependencies with compile or runtime scope (test-scoped dependencies are allowed), and the main sources MUST NOT use regular-expression APIs, databases, frameworks or encryption mechanisms.

#### Scenario: No runtime dependencies
- **WHEN** the effective dependencies of the Maven project are inspected
- **THEN** every declared dependency has `test` scope

#### Scenario: No regular-expression APIs in main sources
- **WHEN** the main sources are inspected
- **THEN** they contain no use of `java.util.regex`, `String.matches`, `replaceAll` or `replaceFirst`
