# project-build Specification

## Purpose
Defines how the Amusement Park project is structured and built: a Maven project created from the quickstart archetype, compiled for JDK 21, that must always build with Maven.

## Requirements

### Requirement: Maven quickstart project structure
The repository root SHALL contain a Maven project created from the Apache `maven-archetype-quickstart` archetype, with a `pom.xml` at the root, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` exist and the sources follow the quickstart archetype layout

### Requirement: JDK 21 build target
The Maven build SHALL compile production and test sources for Java 21.

#### Scenario: Compiler release is 21
- **WHEN** the effective build configuration in `pom.xml` is inspected
- **THEN** the Java compiler release is 21

### Requirement: Project compiles and packages with Maven
The project SHALL compile and package successfully with `mvn package`, running its tests without failures.

#### Scenario: Successful package build
- **WHEN** `mvn package` is executed in the repository root with a JDK supporting release 21
- **THEN** the build finishes with `BUILD SUCCESS` and all tests pass

### Requirement: Milestone commit for project initialization
The `pom.xml` and the basic project structure SHALL be committed to the repository in a commit whose message is exactly `[F0-C01] Initialize Maven project with JDK 21`.

#### Scenario: Mandatory commit exists
- **WHEN** the Git history of the branch is inspected
- **THEN** a commit with message exactly `[F0-C01] Initialize Maven project with JDK 21` exists and contains `pom.xml` and the basic project structure

### Requirement: Executable JAR packaging
`mvn package` SHALL produce the JAR file `target/amusement-park.jar` whose manifest declares the application entry point, so that it can be started with `java -jar` independently of the development environment.

#### Scenario: Package produces runnable JAR
- **WHEN** `mvn package` is executed in the repository root
- **THEN** `target/amusement-park.jar` exists and `java -jar target/amusement-park.jar` starts the application without requiring a classpath or main class argument

### Requirement: Delivery 1 technical restrictions
The Delivery 1 application SHALL use only the Java 21 standard library, with no databases, frameworks, external runtime libraries, regular expressions or encryption mechanisms, and SHALL keep the user class, the main class and the persistence class separate.

#### Scenario: Only standard library at runtime
- **WHEN** the `pom.xml` dependencies are inspected
- **THEN** the only dependencies are test-scoped (JUnit) and production code imports only `java.*` packages and project classes

#### Scenario: No regular expressions in production code
- **WHEN** the production sources are inspected
- **THEN** no regular-expression API (`Pattern`, `Matcher`, `String.matches`, `replaceAll`, regex-based `split` patterns) is used; `split` is used only with single literal separator characters

### Requirement: Delivery 1 commit roadmap
The Git history SHALL contain, in this order, commits with exactly the messages `[F0-C01] Initialize Maven project with JDK 21`, `[F0-C02] Configure executable JAR`, `[F1-C01] Add user model`, `[F1-C02] Add user data validation`, `[F1-C03] Implement user creation`, `[F1-C04] Add user persistence`, `[F1-C05] Implement user deletion`, `[F1-C06] Implement user update`, `[F1-C07] Add command file input`, `[F1-C08] Add help and exit commands` and `[F1-C09] Complete delivery 1 integration`.

#### Scenario: Roadmap commits present in order
- **WHEN** `git log --reverse --format=%s` is inspected
- **THEN** the eleven messages appear in the listed order (other commits may appear between them)

### Requirement: Clean build and IDE-independent execution
The project SHALL build from a clean checkout with `mvn clean package` and the resulting JAR SHALL run with `java -jar target/amusement-park.jar` interactively and with an external `.txt` command file, without an IDE, source changes or extra configuration.

#### Scenario: Statement session from a file
- **WHEN** the JAR is run from a clean directory with the statement's example `input.txt` (two creates, two updates, a delete, `HELP`, `EXIT`)
- **THEN** it prints `USER CREATED`, `USER CREATED`, `USER UPDATED`, `USER UPDATED`, `USER DELETED`, the help text and the farewell, and only Ana (with the new phone and email) remains stored
