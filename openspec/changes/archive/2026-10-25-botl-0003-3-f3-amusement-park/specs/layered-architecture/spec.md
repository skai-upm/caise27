# Spec Delta

## Purpose

Keeps presentation, business and persistence responsibilities physically separated in distinct Java packages while preserving command behavior.

## ADDED Requirements

### Requirement: Layer packages
The source code SHALL be organised in separate packages for the presentation layer (command-based interaction), the business layer (model, domain rules, controllers) and the persistence layer (repositories and file access), with subpackages where useful. No class SHALL be in the default package.

#### Scenario: Packages exist
- **WHEN** `src/main/java` is inspected
- **THEN** it contains `es/upm/parque/presentation`, `es/upm/parque/business` and `es/upm/parque/persistence`, and no `.java` file sits directly in `src/main/java`

### Requirement: Layer responsibilities
Presentation classes (commands) SHALL NOT read or write persistence files directly, and domain model classes SHALL NOT perform file reading or writing; only persistence classes SHALL access the data files.

#### Scenario: No file access outside persistence
- **WHEN** the presentation and business model packages are searched for file I/O APIs (`java.nio.file.Files`, readers/writers on data files)
- **THEN** none is used for persistence data

### Requirement: Behavior preserved
The reorganisation SHALL NOT change the syntax or behavior of any E1 or E2 command, and the project SHALL still build with `mvn package`.

#### Scenario: Previous commands still work
- **WHEN** an E1/E2 command file (users, groups, tickets, group tickets, attractions, SYSTEM commands) is executed after the reorganisation
- **THEN** each command produces the same result as before
