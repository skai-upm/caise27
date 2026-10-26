# project-build Specification

## Purpose


Defines how the amusement park application is built, tested and packaged so that every delivery starts from a reproducible Java 21 Maven project with an executable JAR.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project generated from the `org.apache.maven.archetypes:maven-archetype-quickstart` archetype and SHALL keep its standard layout: a `pom.xml` at the repository root, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` exist following the quickstart archetype structure

### Requirement: Java 21 compilation
The build SHALL compile production and test sources targeting Java 21.

#### Scenario: Compiler targets Java 21
- **WHEN** the project is compiled with Maven on a JDK able to target release 21
- **THEN** compilation succeeds and the produced class files target Java 21

### Requirement: Test execution
The project SHALL include the basic unit-test structure from the archetype, and `mvn test` SHALL compile the project and run its tests successfully.

#### Scenario: mvn test succeeds
- **WHEN** `mvn test` is run at the repository root
- **THEN** the build finishes with success and at least one test is executed with no failures

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class and that contains the project's runtime dependencies, so the JAR can be launched with `java -jar` without any additional classpath.

#### Scenario: Run packaged JAR
- **WHEN** `mvn package` is run and then `java -jar target/parque.jar` is executed
- **THEN** the application starts through its main class and exits normally

#### Scenario: Dependencies available at runtime
- **WHEN** the packaged JAR creates a user (which hashes the password with the BCrypt library)
- **THEN** the command succeeds without a missing-class error

### Requirement: No domain functionality
This initial project SHALL NOT contain application-domain functionality; the main class only provides an entry point.

#### Scenario: Entry point only
- **WHEN** the packaged application is executed
- **THEN** it performs no domain operation (no users, tickets, attractions or persistence) and terminates normally

### Requirement: BCrypt dependency
`pom.xml` SHALL declare the BCrypt library used to hash passwords as a Maven dependency with compile scope.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the `org.mindrot:jbcrypt` dependency
