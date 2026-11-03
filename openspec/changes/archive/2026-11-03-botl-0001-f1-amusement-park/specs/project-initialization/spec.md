# Spec Delta

## Purpose

Defines the mandatory Java 21 Maven project structure on which every Amusement Park delivery is built and evaluated (source F0-C01, section 2.1).

## ADDED Requirements

### Requirement: Java 21 Maven Quickstart project
The project SHALL be a Maven project created from the Apache Maven Quickstart archetype, using the standard Maven directory structure (`pom.xml` at the root, production sources under `src/main/java`, tests under `src/test/java`) and configured to compile for JDK 21.

#### Scenario: Project builds and tests with Maven
- **WHEN** `mvn test` is run on a clean checkout with a JDK able to target Java 21 and Maven installed
- **THEN** compilation succeeds and the baseline tests pass

#### Scenario: Standard layout is present
- **WHEN** the repository layout and `pom.xml` are inspected
- **THEN** they follow the Maven Quickstart structure and the compiler release is 21

### Requirement: Non-standard project structure is not used
The project SHALL NOT use a build tool or directory structure other than the standard Maven layout in this or any later delivery.

#### Scenario: No alternative build structure
- **WHEN** the repository is inspected
- **THEN** there is a single Maven `pom.xml` at the root and no alternative build descriptors for the application
