# component-wiring Specification

## Purpose
Defines how the application's components are created and connected: unique controller instances, repositories created at start-up and injected, and controller collaborations established during initialization.

## Requirements

### Requirement: Singleton controllers
Each business controller that represents a single management responsibility (users, groups, tickets, attractions, invoices) SHALL have exactly one instance, obtained through a class-level access method; its constructor SHALL NOT be accessible from other classes.

#### Scenario: Same instance everywhere
- **WHEN** the user controller instance is requested twice
- **THEN** both requests return the same object

### Requirement: Repositories created at start-up and injected
Repositories SHALL NOT be singletons. The application SHALL create the repository instances during initialization, with the file locations of the current execution mode, and inject them into the controllers that use them.

#### Scenario: Normal mode
- **WHEN** the application starts without `--fly`
- **THEN** controllers are configured with repositories reading and writing files in `data/`

#### Scenario: Fly mode
- **WHEN** the application starts with `--fly`
- **THEN** the same controllers, without any logic change, are configured with repositories using the temporary directory

### Requirement: Collaborations configured at initialization
When a controller needs another controller or service, the dependency SHALL be provided during application initialization; operations SHALL NOT create or look up their collaborators.

#### Scenario: Ticket rules use the configured user controller
- **WHEN** the ticket controller is configured with the user and group controllers at start-up
- **THEN** ticket operations use exactly those instances, and deletion checks for users and groups are registered on them

### Requirement: Reconfiguration replaces state
Configuring a controller again (for example a new execution with a different data directory) SHALL replace its repository, reload its data from that repository and discard rules registered by the previous configuration.

#### Scenario: Second configuration
- **WHEN** the user controller is configured with a repository over an empty file after having been configured with one containing users
- **THEN** it holds no users and no deletion checks from the previous configuration
