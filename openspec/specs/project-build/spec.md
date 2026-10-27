# project-build Specification

## Purpose

Defines how the amusement park application is structured and built: a Maven project with the quickstart archetype layout, targeting JDK 21, that every later delivery extends.

## Requirements

### Requirement: Maven quickstart project structure
The project SHALL be a Maven project created with the Apache Maven quickstart archetype, with `pom.xml` at the repository root, production sources under `src/main/java` and test sources under `src/test/java`.

#### Scenario: Standard layout present
- **WHEN** the repository root is inspected
- **THEN** it contains `pom.xml`, a `src/main/java` source tree and a `src/test/java` test tree

#### Scenario: No default package
- **WHEN** the Java sources are inspected
- **THEN** every class is declared inside a named package

### Requirement: JDK 21 target
The Maven build SHALL be configured to compile the sources for Java release 21.

#### Scenario: Compiler release configured
- **WHEN** the effective `pom.xml` configuration is inspected
- **THEN** the Java compiler release level is 21

### Requirement: Compilable with Maven
The project SHALL compile and pass its tests using Maven without additional manual setup beyond a JDK 21 (or newer) and Maven.

#### Scenario: Package build succeeds
- **WHEN** `mvn package` is executed at the repository root
- **THEN** the build finishes successfully, compiling the sources and running the unit tests without failures

### Requirement: Executable JAR
Running `mvn package` SHALL produce an executable JAR in `target/` whose manifest declares the application entry point, so that it can be started with `java -jar` without an IDE or extra classpath configuration.

#### Scenario: JAR produced by package
- **WHEN** `mvn package` is executed at the repository root
- **THEN** the executable JAR `target/amusement-park.jar` exists

#### Scenario: Interactive execution
- **WHEN** `java -jar target/amusement-park.jar` is started without arguments and `HELP` then `EXIT` are typed on standard input
- **THEN** the application waits for and processes the commands from standard input, printing the help text and ending normally

#### Scenario: Command file execution
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run with a file containing `HELP` and `EXIT`
- **THEN** the application processes the commands contained in `input.txt` and ends normally

### Requirement: Delivery 1 technical restrictions
The application SHALL use only the Java standard library at runtime and SHALL NOT use databases, frameworks, external libraries, regular expressions or encryption mechanisms. Commands SHALL be processed with basic String operations and `split`, persistence SHALL be file based, and the user, main and persistence responsibilities SHALL be in separate classes with encapsulated attributes.

#### Scenario: No runtime dependencies
- **WHEN** the Maven dependencies with compile or runtime scope are listed
- **THEN** there are none (test-scope dependencies only)

#### Scenario: No forbidden mechanisms in sources
- **WHEN** the production sources are inspected
- **THEN** they contain no use of `java.util.regex`, `String.matches`/`replaceAll`, JDBC, object serialization or encryption APIs

### Requirement: Build from scratch
A fresh clone of the repository SHALL build with `mvn package` and produce the executable JAR without IDE files or additional configuration.

#### Scenario: Fresh clone build
- **WHEN** the repository is cloned into an empty directory and `mvn package` is run there
- **THEN** the build succeeds and `java -jar target/amusement-park.jar input.txt` processes the command file

### Requirement: Delivery 2 technical restrictions
The application SHALL keep the Delivery 1 restrictions and represent relationships with objects and collections in memory in the required directions: attractions know their users but users do not know attractions; individual tickets know their user but users do not store tickets; group tickets are composed of individual tickets and reference their group while it exists; groups and users do not reference tickets. Ticket types SHALL share a common abstraction, and a single central controller instance SHALL exist.

#### Scenario: Relationship directions in the code
- **WHEN** the fields of `User`, `Group`, `IndividualTicket`, `GroupTicket` and `Attraction` are inspected
- **THEN** `User` has no ticket, group or attraction fields; `Group` has no ticket fields; `IndividualTicket` has a `User` field; `GroupTicket` has a `Group` field and a collection of `IndividualTicket`; `Attraction` has a collection of `User`

#### Scenario: Common ticket abstraction
- **WHEN** the ticket classes are inspected
- **THEN** `IndividualTicket` and `GroupTicket` both extend the abstract `Ticket`
