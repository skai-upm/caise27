# Design

## Decisions

- **`UserField` enum** has `parse(text)`, returning `null` for unknown names. `DNI` is detected separately so the error message can be specific.
- **`updateUser`** works in two phases: authenticate and validate the new value first, then apply it. For EMAIL it re-keys the `users` map, `Group.replaceMember`, `IndividualTicket.changeUserEmail` and `Attraction.replaceOccupant` (package-private helpers). Because users are referenced by e-mail, this is what keeps the model consistent until e-mail references are replaced by objects.
- **PASSWORD** is validated with `UserValidator.isValidPassword` and stored with `protectPassword`.
- **`IndividualTicket.userEmail`** becomes non-final with a package-private mutator; persistence writes the current value.
