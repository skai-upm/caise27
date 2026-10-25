# layered-architecture Specification

## Purpose

Defines how the source code is physically separated into presentation, business and persistence packages.

## Requirements

### Requirement: Layered packages
Production classes SHALL be organized in named packages: `es.upm.parque.presentation` (command interaction), `es.upm.parque.business` with subpackages for the model, controllers and other domain rules, and `es.upm.parque.persistence` (repositories and file access). The entry point and startup wiring MAY live in `es.upm.parque`. The default package SHALL NOT be used.

#### Scenario: Packages present
- **WHEN** the source tree is inspected
- **THEN** classes exist under `presentation`, `business` and `persistence` packages and no class is in the default package

### Requirement: Responsibilities respect the layers
Command handling classes SHALL NOT read or write persistence files, and domain model classes SHALL NOT perform file reading or writing; only persistence classes access data files.

#### Scenario: No file access outside persistence
- **WHEN** the presentation and business packages are inspected for file I/O on data files
- **THEN** none is found; data files are only read and written by persistence classes

### Requirement: Behavior preserved by the reorganization
The reorganization SHALL NOT change the syntax or behavior of any E1 or E2 command, and the project SHALL still build with `mvn package`.

#### Scenario: E1 and E2 commands still work
- **WHEN** the E1 and E2 command sequences used before the reorganization are executed
- **THEN** they produce the same results
