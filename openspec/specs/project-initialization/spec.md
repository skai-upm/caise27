# project-initialization Specification

## Purpose

Defines the mandated project skeleton: a Java 21 Maven project created from the Apache Maven Quickstart archetype with the standard directory layout, compilable and testable with Maven.

## Requirements

### Requirement: Java 21 Maven Quickstart project
The project SHALL be a Maven project that follows the Apache Maven Quickstart archetype layout (`pom.xml` at the root, production code under `src/main/java`, tests under `src/test/java`) and SHALL be configured to compile for JDK 21. (F0-C01)

#### Scenario: Clean build and tests
- **WHEN** `mvn test` runs on a clean checkout with a JDK able to target Java 21
- **THEN** compilation succeeds and the baseline tests pass

#### Scenario: Layout inspection
- **WHEN** the project configuration and directory layout are inspected
- **THEN** `pom.xml` declares Java release 21 and the sources follow the standard Maven Quickstart structure
