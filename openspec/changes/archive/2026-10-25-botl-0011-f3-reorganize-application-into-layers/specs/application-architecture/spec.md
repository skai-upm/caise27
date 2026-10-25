# Spec Delta

## Purpose

Defines how the application's code is organised into presentation, business and persistence layers and which dependencies between them are allowed.

## ADDED Requirements

### Requirement: Layered packages
The production code SHALL be organised into separate Java packages for the presentation layer (command-based interaction), the business layer (domain model, domain rules and controllers, in sub-packages) and the persistence layer (repositories and file access). No class SHALL be in the default package.

#### Scenario: Packages present
- **WHEN** the source tree is inspected
- **THEN** it contains the packages `es.upm.parque.presentation`, `es.upm.parque.business.model`, `es.upm.parque.business.controller` and `es.upm.parque.persistence`, and no source file lacks a package declaration

### Requirement: Layer dependencies
Presentation classes SHALL NOT access persistence classes or files directly; business model classes SHALL NOT read or write files nor depend on persistence or presentation; persistence classes SHALL depend only on the business model. Only the application entry point wires repositories into controllers.

#### Scenario: Commands do not access files
- **WHEN** the imports of the presentation and business model packages are inspected
- **THEN** none of them references `es.upm.parque.persistence`, `java.nio.file.Files` or other file I/O classes

### Requirement: Behavior preserved
The reorganisation SHALL NOT change the syntax or behavior of any Delivery 1 or Delivery 2 command, the stored file formats or the execution modes, and `mvn package` SHALL still produce the executable JAR.

#### Scenario: Existing commands still work
- **WHEN** the full test suite and a command file exercising users, groups, tickets, group tickets, attractions, `SYSTEM DATE`, `--fly`, `HELP` and `EXIT` are run after the reorganisation
- **THEN** all tests pass and the responses are identical to those before it
