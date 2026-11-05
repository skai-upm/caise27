# Design

## Context

Users exist only as e-mails behind `UserDirectory` (exists, dniOf). Layering rules forbid file I/O in business. See proposal.md - Why.

## Decisions

- **`business.User`** has seven private fields. `dni` is `final` and has no setter. Every constructor argument and setter rejects `null` (`Objects.requireNonNull`). Format validation is left to the valid-users milestone, so no business rule is invented here.
- **Password field** holds whatever protected value the caller provides. Hashing stays in `PasswordHasher` / `ParkController.protectPassword`, so `User` doesn't depend on the BCrypt library.
- **Identity**: `equals`/`hashCode` aren't overridden. Users are distinct objects, and uniqueness rules (DNI, e-mail) belong to registration.
- **Tests**: reflection verifies that all fields are private, `dni` is final, and there is no `setDni`. Construction round-trips and null rejection are also tested. `LayeringTest` already forbids file I/O in business.
