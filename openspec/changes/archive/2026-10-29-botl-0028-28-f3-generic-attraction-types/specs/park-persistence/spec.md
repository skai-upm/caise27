# Spec Delta

## ADDED Requirements

### Requirement: Attraction type persisted
Attractions SHALL be stored with their type and reconstructed with it; stored attractions without a type SHALL be read as GENERAL.

#### Scenario: Old attractions file
- **WHEN** `attractions.txt` contains `A01;Dragon Rojo;20` without a type
- **THEN** after startup `A01` is a GENERAL attraction
