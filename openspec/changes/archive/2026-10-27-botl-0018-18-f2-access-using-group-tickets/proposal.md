# Proposal

## Why

Milestone F2-C07 extends the single access operation to group tickets — all members enter together or nobody does — and adds `ATTRACTION EXIT` and `ATTRACTION STATUS` so the results of entering and leaving can be verified.

## What Changes

- `ATTRACTION ENTER` accepts group tickets through the same mechanism: every user of the group ticket's composition enters together; if the attraction lacks room for all of them (or any of them is already inside), nobody enters.
- `ATTRACTION EXIT;AttractionIdentifier;Email` removes that user from the attraction and frees the place; members who entered with a group ticket leave individually.
- `ATTRACTION STATUS;AttractionIdentifier` shows identifier, name, maximum capacity, number of users inside, available places and the users inside, computed from the attraction's own state, regardless of how users entered.
- Commit with the exact message `[F2-C07] Add group attraction access`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `attraction-access`: entry with group tickets (atomic), and leaving attractions.
- `attractions`: attraction status query.

## Impact

- `AttractionCommands` (`enter` drops the temporary group-ticket rejection; new `exit` and `status`); HELP lists the commands.
- Ambiguities flagged: STATUS output format (free per source) is a multi-line block `Attraction: …`, `Name: …`, `Capacity: …`, `Users inside: …`, `Available places: …`, `Users: <emails or (none)>`; EXIT response `EXITED <attraction>: <email>`; EXIT for a user not inside is an error.
