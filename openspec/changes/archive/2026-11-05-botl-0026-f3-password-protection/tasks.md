## 1. Build

- [x] 1.1 Add the `org.mindrot:jbcrypt:0.4` dependency and `maven-shade-plugin` (Main-Class manifest), so `target/amusement-park.jar` contains jBCrypt.

## 2. Password protection

- [x] 2.1 Add the `PasswordProtector` interface and `BCryptPasswordProtector`: configurable cost, `protect` and `matches`, with malformed values never matching.
- [x] 2.2 Inject the protector into `UserManager` through `ParkConfiguration`, with the default cost 10 and an overload for an injected protector.
- [x] 2.3 `UserManager`:
  - validate, then protect, on create and on PASSWORD update;
  - authenticate with `matches`.
- [x] 2.4 `UserValidator.isProtectedPassword`: a BCrypt format check without regex, used by `isValid` for stored users.

## 3. Verification and documentation

- [x] 3.1 Update the existing tests:
  - plain-password assertions;
  - plain-text lines in load tests;
  - `TestSupport` with cost 4.
- [x] 3.2 Add `PasswordProtectionTest`:
  - BCrypt value in the file, without the plain password;
  - verification after restart;
  - the protected value rejected as a password;
  - a password change re-protected;
  - an invalid new password;
  - a malformed stored value;
  - a plain-text line ignored on load;
  - the injected protector used.
- [x] 3.3 Update README (password storage, migration note, dependency).
- [x] 3.4 Run `mvn package` and an end-to-end JAR regression: output identical to the baseline except where the file now holds BCrypt values. Also confirm that jBCrypt is inside the JAR.
