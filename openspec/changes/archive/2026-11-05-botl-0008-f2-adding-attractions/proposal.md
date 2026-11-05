# Proposal

## Why

Milestone F2-C05 adds park attractions, each with a unique identifier, a name, a maximum capacity and the collection of users currently inside it. `ATTRACTION LIST` lets you check who is inside. Access control (the next milestones) moves users in and out of attractions.

## What Changes

- Add attractions to central management.
- `ATTRACTION CREATE;Identifier;Name;Capacity` creates an attraction. The identifier must be unique, the name non-empty and the capacity a positive whole number. It starts with no occupants.
- `ATTRACTION LIST;AttractionIdentifier` is read-only. It shows the e-mails of the users currently inside, or a controlled "no users" result. Unknown attractions are reported as errors.
- The attraction→occupant relationship is one-way: attractions reference their occupants, and users never reference attractions.
- `HELP` lists the ATTRACTION commands.

### Out of scope
- Entering or leaving attractions, enforcing capacity on entry, and persistence (later milestones).

### Deferred
- There is no user model in the code base yet, so occupants are referenced by e-mail, the user identity used in commands. Switching to object references is up to the user-model milestone.

## Capabilities

### New Capabilities
- `attractions`: Park attractions (identity, name, capacity, current occupants), how they are created, and the read-only occupant listing.

### Modified Capabilities
<!-- None -->

## Impact

- New `Attraction` and `AttractionCommands`.
- `ParkController` gains attraction storage.
- `HELP` text and tests.
