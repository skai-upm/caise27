# project-initialization Specification

## Purpose

Defines the mandatory project baseline: a Java 21 Maven project created with the Maven Quickstart archetype and its standard directory structure (F0-C01).

## Requirements

### Requirement: Java 21 Maven Quickstart project
The project SHALL be a Maven project targeting JDK 21 that follows the `maven-archetype-quickstart` standard layout: a root `pom.xml`, production sources under `src/main/java` and test sources under `src/test/java`. The project SHALL compile and run its tests with Maven.

#### Scenario: Clean build and tests
- **WHEN** `mvn test` is run on a clean checkout with JDK 21 and Maven installed
- **THEN** compilation succeeds and the test suite passes

#### Scenario: Standard layout and Java release
- **WHEN** the project configuration is inspected
- **THEN** `pom.xml` exists at the repository root, declares Java release 21, and sources and tests are located in `src/main/java` and `src/test/java`
