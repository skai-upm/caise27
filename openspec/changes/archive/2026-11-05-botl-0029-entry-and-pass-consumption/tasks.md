# Tasks

## 1. Entry with passes

- [x] 1.1 Add `PassLookup`, `EntryResult`, `TicketAccess.individualTickets()` and the pass step in `Attraction.enter` with `SpecialAttraction` rules; verify unit tests for atomicity (no change on any failure)
- [x] 1.2 Extend `AttractionService.enter` and `ATTRACTION ENTER` with the optional `SPEED` element and pass reporting; verify command tests for individual/group SPEED, special entry, and all rejections

## 2. Integration

- [x] 2.1 Run `mvn clean package` and the JAR with SPEED and SPECIAL entry sequences
