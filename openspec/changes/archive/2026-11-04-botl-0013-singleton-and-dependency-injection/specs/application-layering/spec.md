## MODIFIED Requirements

### Requirement: Layered packages
The application SHALL organise its production classes into distinct Java packages for presentation (`com.parque.presentation`), business (`com.parque.business` with subpackages for model, services and controller) and persistence (`com.parque.persistence`); no class SHALL be in the default package, and only the bootstrap class `App` and the composition root `ApplicationContext` MAY reside directly in `com.parque`.

#### Scenario: Package structure
- **WHEN** the production source tree is inspected
- **THEN** every class belongs to one of `com.parque.presentation`, `com.parque.business.model`, `com.parque.business.service`, `com.parque.business.controller`, `com.parque.persistence`, or is `com.parque.App` or `com.parque.ApplicationContext`, and no source file lacks a package declaration
