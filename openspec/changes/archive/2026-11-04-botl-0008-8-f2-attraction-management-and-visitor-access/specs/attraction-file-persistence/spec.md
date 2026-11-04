# Spec Delta

## Purpose

Defines how attraction definitions are stored in a plain text file and restored on startup, while occupancy is never persisted.

## ADDED Requirements

### Requirement: Attraction records
The system SHALL store each attraction on one line of `attractions.txt` as `Id;Nombre;Capacidad`, rewrite the file after every successful creation, and restore the attractions on startup with no users present; a missing file SHALL mean no attractions and malformed lines SHALL be skipped with a warning.

#### Scenario: File content and restoration
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` succeeds and the application restarts
- **THEN** `attractions.txt` contains `A01;Tornado;20` and attraction `A01` exists again, empty, with capacity 20
