# Spec Delta

## Purpose

Defines the mandated project baseline: a Java 21 Maven project created from the Apache Maven Quickstart archetype with the standard Maven directory structure, compilable and testable with Maven.

## ADDED Requirements

### Requirement: Java 21 Maven Quickstart project
The project SHALL be a Maven project created from the Apache Maven Quickstart archetype (`maven-archetype-quickstart`), configured to compile for JDK 21, with its `pom.xml` and standard Maven directory structure (`src/main/java`, `src/test/java`) at the repository root.

#### Scenario: Build baseline succeeds
- **WHEN** `mvn test` runs on a clean checkout with JDK 21 and Maven installed
- **THEN** compilation and the baseline tests succeed

#### Scenario: Mandated layout and Java level
- **WHEN** the project configuration is inspected
- **THEN** `pom.xml` declares Java release 21 and the sources follow the Maven Quickstart standard layout
