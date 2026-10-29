# project-build Specification

## Purpose

Defines the Maven project structure, Java version and build guarantees of the Amusement Park application so that every later delivery builds on an accepted, compilable project.

## Requirements

### Requirement: Maven quickstart project structure
The repository SHALL contain a Maven project created from the apache-maven-quickstart archetype, with a `pom.xml` at the project root, production sources under `src/main/java` and test sources under `src/test/java`. Sources SHALL NOT be placed in the default Java package.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** it contains `pom.xml`, a `src/main/java` directory with at least one Java class inside a named package, and a `src/test/java` directory with at least one test class

### Requirement: JDK 21 configuration
The Maven project SHALL be configured to compile Java sources for JDK 21.

#### Scenario: Compiler targets Java 21
- **WHEN** the `pom.xml` is inspected
- **THEN** the compiler release (or equivalent source/target configuration) is set to 21

#### Scenario: Compiled classes are Java 21 bytecode
- **WHEN** the project is compiled with Maven
- **THEN** the produced class files have the Java 21 class-file major version (65)

### Requirement: Compilable with Maven
The project SHALL compile and pass its tests using Maven without an IDE or additional configuration.

#### Scenario: Successful package build
- **WHEN** `mvn package` is run from the repository root
- **THEN** the build finishes with `BUILD SUCCESS` and all tests pass

### Requirement: Executable JAR
Building the project with `mvn package` SHALL produce an executable JAR in `target/` whose manifest declares the application entry point, so the application starts with `java -jar` without an IDE, source changes or additional configuration.

#### Scenario: Interactive execution of the JAR
- **WHEN** `java -jar target/amusement-park.jar` is run without arguments and `HELP` then `EXIT` are typed on standard input
- **THEN** the application prints the help text and terminates normally

#### Scenario: Command-file execution of the JAR
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run with a text file containing commands
- **THEN** the application processes the commands of `input.txt` in order and terminates

#### Scenario: Build from scratch
- **WHEN** `mvn clean package` is run on a fresh checkout
- **THEN** the build succeeds and `target/amusement-park.jar` exists

### Requirement: Java standard library only
The application's runtime code SHALL depend only on the Java standard library and, from Delivery 3, on the BCrypt library used to protect passwords: no databases, frameworks, other external libraries or regular expressions are used. The BCrypt library SHALL be packaged in the executable JAR.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** every declared dependency has `test` scope except the BCrypt library `org.mindrot:jbcrypt`

#### Scenario: JAR self-contained
- **WHEN** `java -jar target/amusement-park.jar` runs on a machine with only a Java runtime
- **THEN** the BCrypt classes are found inside the JAR

### Requirement: PlantUML class model
The repository SHALL contain the PlantUML source file `uml/model.puml` with a class diagram of the implemented design showing its classes, interfaces and relevant enumerations, relevant attributes, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities, without methods or constructors. The diagram SHALL correspond to the code: the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions SHALL be represented as implemented.

#### Scenario: Model file present and complete
- **WHEN** the build runs its tests
- **THEN** `uml/model.puml` exists, starts with `@startuml`, ends with `@enduml`, and declares every production class of the application

#### Scenario: Key relationships represented
- **WHEN** `uml/model.puml` is inspected
- **THEN** it declares `IndividualTicket` and `GroupTicket` extending `Ticket`, a composition from `GroupTicket` to `IndividualTicket` with multiplicity `1..*`, and directed associations from `IndividualTicket` to `User`, from `GroupTicket` to `Group` (`0..1`), from `Group` to `User` and from `Attraction` to `User`
