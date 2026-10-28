# Design

## Context

All Delivery 2 features exist (F2-C01…F2-C09). HELP currently uses lowercase/Phase-1 placeholder names for some commands. `App.run(args, stdin, out, workDir)` supports normal and `--fly` modes with `ParkStorage`.

## Decisions

- **HELP text**: replace `HelpCommand.SYNTAX` with the exact §15 list (order as in the source); existing tests iterate `SYNTAX`, so they keep validating every line.
- **`Delivery2IntegrationTest`**: (a) normal mode: writes `data/users.txt` with stored users (as a previous execution would have, since `USER CREATE` does not exist yet), runs a command file covering every Delivery 2 command and several rejections, then restarts and checks the persisted state; (b) `--fly` mode starts from an empty temporary directory by design, so a user-free command file is run once in normal mode (in a fresh working directory) and once with `--fly`, comparing outputs and checking that no temporary directory remains.
- **`ModelStructureTest`**: reflection over `User`, `Group`, `Attraction`, `IndividualTicket`, `GroupTicket` field types (including generic element types of collections) to assert the mandated directions; asserts `ParkController.getInstance()` identity and that the constructor is private.
