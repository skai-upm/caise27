# Tasks

## 1. Model and controller

- [x] 1.1 Add `Attraction` (id, name, positive capacity, ordered unique occupant emails, read-only view, enter/leave primitives), and verify with `AttractionTest` that a new attraction is empty, duplicates are not added and the view is unmodifiable
- [x] 1.2 Add attraction storage and `createAttraction`/`findAttraction` to `ParkController`, with rejection of duplicate ids, blank fields and capacity <= 0, and verify with tests that rejected creations leave state unchanged

## 2. Commands

- [x] 2.1 Add the ATTRACTION CREATE (integer capacity parsing) and ATTRACTION LIST (emails, or `NO USERS IN <id>`) handlers and HELP lines, and verify with `AttractionCommandsTest` the creation, all invalid inputs, empty and non-empty listing without state change, an unknown id, and HELP
- [x] 2.2 Document the ATTRACTION commands in `README.md`, and verify that `mvn clean package` passes and that a JAR run of `ATTRACTION CREATE;A01;Dragon Rojo;20` followed by `ATTRACTION LIST;A01` shows the empty result
