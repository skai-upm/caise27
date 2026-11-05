# Tasks

## 1. Model and store

- [x] 1.1 Create `model.Attraction` (id, name, capacity, occupant list with unmodifiable view, `addOccupant`/`removeOccupant`/`isInside` by DNI) and `repository.AttractionRepository`; add attractions to `ParkController` (reset per execution); verify `AttractionTest` and that `User` has no attraction field

## 2. Commands

- [x] 2.1 Implement `ATTRACTION CREATE` (digit-only positive capacity) and `ATTRACTION LIST` (read-only, empty/controlled result, unknown → ERROR) in `command.AttractionCommands`, dispatched from `CommandProcessor`, and list both in `HELP`; verify `AttractionCommandsTest` for every scenario in the attractions spec (occupants added programmatically for the listing case)

## 3. Integration

- [x] 3.1 End-to-end with the JAR: create A01/A02, duplicate and invalid capacities, list empty and unknown; verify responses and exit status 0; full `mvn package` regression
