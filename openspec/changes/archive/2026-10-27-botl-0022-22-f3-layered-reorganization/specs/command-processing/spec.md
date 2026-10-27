# Spec Delta

## ADDED Requirements

### Requirement: Behaviour preserved after reorganisation
After the layered reorganisation every Delivery 1 and Delivery 2 command SHALL produce the same outputs, persisted data and execution forms as before.

#### Scenario: Delivery 2 session unchanged
- **WHEN** the Delivery 2 integration session is executed after the reorganisation, in normal and `--fly` modes
- **THEN** the output and persisted files are identical to those produced before the reorganisation
