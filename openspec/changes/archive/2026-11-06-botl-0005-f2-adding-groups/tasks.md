# Tasks

## 1. Group model and controller

- [x] 1.1 Add `es.upm.park.model.Group` (id, name, ordered unique member emails), and verify with `GroupTest` that a duplicate member is not added and removal of a non-member reports no change
- [x] 1.2 Add to `ParkController` the group storage, the `userExists` and `groupTicketDates` lookups (defaults: none) and the derived `isGroupLocked`, and verify with tests that the lock is true for a ticket dated today or later, false for only past tickets, and changes when the system date moves
- [x] 1.3 Add the controller operations create/add/remove/delete with all rejections (duplicate id, missing fields, unknown group or user, duplicate member, non-member, locked group, ticket today or later on delete), and verify with `ParkControllerGroupTest` that each rejected operation leaves state unchanged

## 2. Commands

- [x] 2.1 Add the GROUP CREATE/ADD/REMOVE/DELETE handlers and HELP lines in `CommandProcessor`, and verify with `GroupCommandsTest` the confirmations, the `ERROR:` messages for wrong arguments, and the HELP content
- [x] 2.2 Document the GROUP commands in `README.md`, run `mvn clean package`, and verify that all tests pass and that a JAR run of `GROUP CREATE;G01;Familia Garcia`, `GROUP CREATE;G01;X` and `GROUP DELETE;G01` gives a confirmation, an error and a confirmation
