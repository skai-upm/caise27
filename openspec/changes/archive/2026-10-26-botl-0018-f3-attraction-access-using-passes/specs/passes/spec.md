# Spec Delta

## ADDED Requirements

### Requirement: Passes consumed by attraction entry
SpeedPasses and Special Passes SHALL be consumed only by successful attraction entries, and the new consumed status SHALL be persisted immediately.

#### Scenario: Consumption survives a restart
- **WHEN** a SpeedPass is consumed by `ATTRACTION ENTER;A01;T001;SPEED` and the application restarts
- **THEN** that SpeedPass is restored as consumed
