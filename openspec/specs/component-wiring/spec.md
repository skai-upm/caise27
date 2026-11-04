# component-wiring Specification

## Purpose
Defines how the application's components are created and connected: unique controllers are Singletons per execution, while repositories and other collaborators are created and configured at startup and injected into the components that use them.

## Requirements

### Requirement: Singleton main controller
The main controller SHALL guarantee a single instance per execution: it SHALL NOT be instantiable directly, SHALL be initialized once at startup, every element requesting it during the execution SHALL obtain that same instance, and a second initialization while an instance exists SHALL be rejected.

#### Scenario: Same instance everywhere
- **GIVEN** the application has started
- **WHEN** the command processor and any other element request the main controller
- **THEN** they obtain the same instance

#### Scenario: Second initialization rejected
- **GIVEN** the main controller has been initialized for the current execution
- **WHEN** another initialization is attempted
- **THEN** it fails and the existing instance is kept

#### Scenario: Access before startup
- **WHEN** the main controller is requested before it has been initialized
- **THEN** the request fails with an explicit error

#### Scenario: New execution, new instance
- **GIVEN** an execution has ended and released its controller
- **WHEN** a new execution starts in the same JVM
- **THEN** it gets a new controller whose system date is the OS date and whose ticket price is 40.00

### Requirement: Repositories configured at startup and injected
Repositories SHALL be ordinary (non-Singleton) objects created and configured with their file paths at application startup and passed to the services that use them; business operations SHALL NOT construct repositories.

#### Scenario: Normal and --fly paths
- **WHEN** the application starts normally and, separately, with `--fly`
- **THEN** the repositories are created with paths in the working directory and in the temporary directory respectively, and the same controller and service classes and logic are used in both cases

#### Scenario: Replaceable repositories
- **GIVEN** two sets of repositories configured for two different directories
- **WHEN** services are created with each set
- **THEN** each service reads and writes only the files of its own repositories

### Requirement: Collaborators wired during initialization
Dependencies between the controller and services (and between services) SHALL be established when the application is initialized, through constructor or initialization parameters, and SHALL NOT be constructed inside the operations that use them.

#### Scenario: Invoice director injected
- **WHEN** invoices are generated several times in one execution
- **THEN** the controller uses the invoice director injected at startup rather than creating one per operation

#### Scenario: Behaviour unchanged
- **WHEN** the existing E1/E2/F3 command scripts are executed after the change
- **THEN** responses and data files are identical to those before the change
