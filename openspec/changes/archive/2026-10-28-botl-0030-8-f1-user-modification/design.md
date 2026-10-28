# Design

## Context

`User` has setters for every attribute except DNI; `ParkController` indexes users by e-mail; `UserValidator` and `PasswordPolicy` implement the creation rules; `PasswordHasher` is injected. Groups, tickets and passes reference `User` objects, so changing a user's e-mail only requires re-indexing.

## Decisions

- **`business.model.UserField`** enum: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD with `parse(text)` → `InvalidValueException` for anything else (e.g. DNI).
- **`ParkController.updateUser(email, password, fieldText, newValue)`**: find → `NotFoundException`; verify password → `AuthenticationException`; parse field; then per field: NAME/SURNAME/PHONE set; EMAIL → `UserValidator.isValidEmail`, uniqueness against other users, remove old key, set, put new key; PASSWORD → `PasswordPolicy.validate`, `setPasswordHash(hasher.hash(newValue))`; CARD → `UserValidator.isValidCard`. All validation happens before the setter.
- **`UserUpdateCommand`** (4 fields, `USER UPDATED`).
- **UML**: add `enum UserField` and `UserField ..> User : modifiable fields`.
