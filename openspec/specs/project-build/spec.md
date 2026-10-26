# project-build Specification

## Purpose

Defines how the application is built, tested and packaged so every later feature starts from a reproducible Java 21 Maven baseline.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project generated from the `org.apache.maven.archetypes:maven-archetype-quickstart` archetype and SHALL keep its standard directory layout (`pom.xml`, `src/main/java`, `src/test/java`).

#### Scenario: Standard layout present
- **WHEN** a developer inspects the repository root
- **THEN** it contains `pom.xml`, a main source tree under `src/main/java` and a test source tree under `src/test/java`

### Requirement: JDK 21 compilation target
The build SHALL compile the sources for Java release 21.

#### Scenario: Compiling with Maven
- **WHEN** a developer runs `mvn compile` with a JDK 21 or newer
- **THEN** the build succeeds and produces class files targeting Java 21

### Requirement: Basic testing structure
The project SHALL include a unit test source tree with at least one test that runs during `mvn test`.

#### Scenario: Running tests
- **WHEN** a developer runs `mvn test`
- **THEN** the build succeeds and at least one test is executed without failures

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the main class, so it can be launched with `java -jar`.

#### Scenario: Launching the packaged JAR
- **WHEN** a developer runs `mvn package` and then `java -jar` on the produced JAR
- **THEN** the main class runs and the process exits successfully

### Requirement: Password hashing dependency
The build SHALL declare the BCrypt library as a Maven dependency in `pom.xml`, and the executable JAR produced by `mvn package` SHALL include it so that `java -jar` works without additional classpath configuration.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the `org.mindrot:jbcrypt` dependency

#### Scenario: Self-contained JAR
- **WHEN** `mvn package` is run and `java -jar target/parque.jar` executes `USER CREATE` for a valid user
- **THEN** the user is created without class-loading errors

### Requirement: End-to-end delivery integration check
The build SHALL include an automated test that runs a command script covering E1, E2 and E3 commands through the application entry point in file mode, interactive mode and `--fly` mode, and checks the responses, the persistence files of normal mode and the cleanup of `--fly` mode.

#### Scenario: Integration script
- **WHEN** `mvn test` runs
- **THEN** the integration script is executed in all three modes and every expected response is verified

#### Scenario: Packaged application
- **WHEN** `mvn package` is run and `java -jar target/parque.jar --fly <script>` executes the integration script
- **THEN** it produces the same responses as the test run

### Requirement: Run documentation
The repository README SHALL explain how to build the executable JAR, the three execution modes (interactive, command file, `--fly`) and how to list the commands with `HELP` and `HELP EXTENDED`.

#### Scenario: README content
- **WHEN** `README.md` is read
- **THEN** it shows `mvn package`, `java -jar target/parque.jar`, `java -jar target/parque.jar <fichero>`, `--fly`, `HELP` and `HELP EXTENDED`
