# Proposal

## Why

Milestone F2-C07: the same `ATTRACTION ENTER` operation must accept group tickets, letting all members of the composition in together or nobody, and staff must be able to verify the results of entries and exits through `ATTRACTION EXIT` and `ATTRACTION STATUS`.

## What Changes

- `ATTRACTION ENTER` accepts group tickets: all users of the group ticket's individual tickets enter together if the attraction has room for all of them; otherwise none enters. Same mechanism as individual tickets (the temporary group-ticket guard is removed).
- Add `ATTRACTION EXIT;AttractionIdentifier;Email`: removes that user from the attraction's occupants, releasing capacity; users who entered with a group ticket leave one by one with their own email.
- Add `ATTRACTION STATUS;AttractionIdentifier`: shows identifier, name, maximum capacity, number of users inside, available places and the users inside, from the attraction's own state, regardless of how users entered.
- `HELP` lists `ATTRACTION EXIT` and `ATTRACTION STATUS`.
- Commit with the mandatory message `[F2-C07] Add group attraction access`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attraction-access`: group-ticket entry (indivisible), leaving attractions, attraction status.
- `execution-control`: `HELP` also lists the two new commands.

## Impact

- `ParkController.enterAttraction` (guard removed), `AttractionService.exit/status`, processor commands; tests.
- Ambiguities flagged:
  - `ATTRACTION EXIT` matches the email against the users inside the attraction (so it also works if the user's account no longer exists); an email not inside is rejected.
  - Output: entry prints one `USER ENTERED ATTRACTION: <email>` line per user; exit prints `USER LEFT ATTRACTION: <email>`; status is a multi-line block (`Attraction`, `Name`, `Capacity`, `Occupants`, `Available`, `Users`), with `Users: none` when empty.
