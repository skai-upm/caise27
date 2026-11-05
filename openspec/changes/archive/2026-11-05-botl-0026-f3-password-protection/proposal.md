## Why

Delivery 3, milestone F3-C05 (source section 8), requires that user passwords are no longer stored in plain text. A BCrypt library is added through Maven (jBCrypt 0.4). The Delivery 1 password rules are applied before a password is stored, and only the BCrypt-protected value is persisted. `USER DELETE` and `USER UPDATE` verify the entered password against the stored value with the library, and a changed password gets a new protected value. Migration of older user files is not required.

## What Changes

- **Build.**
  - Add the `org.mindrot:jbcrypt:0.4` dependency.
  - Package with `maven-shade-plugin`, so `target/amusement-park.jar` stays runnable with `java -jar` and includes jBCrypt.
- **Password protection.**
  - New `business.PasswordProtector` strategy interface: `protect(plain)` and `matches(plain, protectedValue)`.
  - `BCryptPasswordProtector` implementation with a configurable cost; the default cost is 10.
  - The protector is injected into `UserManager` by `ParkConfiguration`.
- **User management.**
  - `USER CREATE` validates the plain password, then stores `protect(password)`.
  - `USER UPDATE PASSWORD` validates the new value and stores a new protected value.
  - Authentication uses `matches`. Malformed stored values never match.
- **Loading.**
  - `UserValidator.isValid(user)` (used when loading) now requires the password to be a BCrypt-format value. The check is hand-written, because regex is forbidden in main sources.
  - Lines with plain-text passwords from earlier versions are ignored and counted in the existing warning.
- **Specs.**
  - New capability `password-protection`.
  - Modified requirements for:
    - the "standard library only" exception;
    - the executable JAR contents;
    - the users file format and load validation;
    - whole-user validation;
    - deletion and update authentication.

## Capabilities

### New Capabilities
- `password-protection`: BCrypt storage, verification, re-protection on change, and an injected protection behaviour.

### Modified Capabilities
- `project-initialization`: jBCrypt is the only allowed runtime dependency.
- `executable-application`: the JAR bundles the runtime dependency.
- `user-persistence`: the password field holds a BCrypt value, and loaded lines must hold one.
- `user-validation`: whole-user validation checks the BCrypt format of stored passwords.
- `user-deletion`: credentials are verified with jBCrypt.
- `user-modification`: authentication is verified with jBCrypt.

## Impact

- Code:
  - `pom.xml`;
  - in `business`: `PasswordProtector`, `BCryptPasswordProtector`, `UserManager` and `ParkConfiguration`;
  - in `business.model`: `UserValidator`.
- Tests that read or write plain-text passwords in user files, or that compare stored passwords, are updated. Tests use a low BCrypt cost through injection, to keep the suite fast.
- The UML model, README and `.gitignore` do not change, apart from README notes. Existing `users.txt` files with plain-text passwords will have those users ignored, which the source explicitly allows.
