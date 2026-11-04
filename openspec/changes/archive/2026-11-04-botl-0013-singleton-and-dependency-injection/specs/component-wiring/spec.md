# Spec Delta

## Purpose

Defines how the application's components are created and connected: which controller is unique per execution, and how services and repositories are configured at startup and injected so that they stay replaceable.

## ADDED Requirements

### Requirement: Unique main controller
The main park controller SHALL exist at most once per execution. It SHALL be created explicitly during application startup, every component that needs it SHALL obtain that same instance, a second creation during the same execution SHALL be refused, and requesting it before creation SHALL fail. When an execution ends, the instance SHALL be released so that a new execution can create its own.

#### Scenario: Same instance for every requester
- **GIVEN** the application has started
- **WHEN** the presentation layer and any other component request the main controller
- **THEN** they all obtain the same instance

#### Scenario: Second creation refused
- **GIVEN** the main controller has been created for the current execution
- **WHEN** another creation is attempted in the same execution
- **THEN** it is refused with an error and the existing instance remains in use

#### Scenario: New execution
- **GIVEN** an execution has ended
- **WHEN** the application is run again in the same JVM
- **THEN** a new main controller is created for the new execution

### Requirement: Services and repositories injected at startup
Repositories and business services SHALL be created and configured once during application startup and passed to the components that use them. Business operations SHALL NOT construct repositories, services or other controllers. Collaborators of the main controller, including the invoice director, SHALL be supplied when it is created.

#### Scenario: No local construction
- **WHEN** a business operation such as generating an invoice or deleting a user runs
- **THEN** it uses collaborators received at startup and does not create a repository, service or controller

### Requirement: Replaceable repositories
Repositories SHALL NOT be Singletons. Several repository instances with different file locations SHALL be able to coexist, and the controller and services SHALL work unchanged with whichever instances they are given.

#### Scenario: Normal and --fly configuration
- **GIVEN** one execution started normally and one started with `--fly`
- **WHEN** the repositories are configured at startup
- **THEN** they use different file paths, the same controller and service code runs in both, and the command results are the same for the same commands on empty data

#### Scenario: Independent repository instances
- **WHEN** two user repositories are created with different files
- **THEN** each reads and writes only its own file
