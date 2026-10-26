# Spec Delta

## ADDED Requirements

### Requirement: Unique controllers
Each business controller representing a single management responsibility (users, groups, tickets, attractions, invoices) SHALL have exactly one instance in the application, obtained through a single access point; it SHALL NOT be instantiable from other classes.

#### Scenario: Same instance everywhere
- **WHEN** the user controller is requested from two different places
- **THEN** both receive the same instance

### Requirement: Dependencies injected at startup
Repositories and every collaborator a controller needs (other controllers, the system date, the invoice builder) SHALL be created or obtained during application initialization and injected into the controller; controllers SHALL NOT construct their dependencies themselves. Repositories SHALL NOT be singletons.

#### Scenario: Fly mode without controller changes
- **WHEN** the application starts with `--fly`
- **THEN** the same controllers are configured with repositories pointing to the temporary directory and behave exactly as in normal mode

#### Scenario: Unconfigured controller
- **WHEN** a controller is used before being configured
- **THEN** the operation fails with an error stating the controller is not configured
