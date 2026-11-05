# Tasks

## 1. User model

- [x] 1.1 Create `es.upm.parque.model.User` with private fields for first name, surnames, DNI (final), email, password, telephone and card number, an all-attributes constructor, getters for all seven and setters for all but DNI; verify `mvn compile` succeeds
- [x] 1.2 Add `UserTest` covering: all attributes returned as supplied; email change via setter leaves others unchanged; two users are independent; verify with `mvn test`
- [x] 1.3 Add a reflection test asserting `User` has no non-private fields, the DNI field is final, and there is no `setDni` method; verify with `mvn test`

## 2. Integration

- [x] 2.1 Verify `User` contains no `java.io`/`java.nio` usage (grep) and `App` is unchanged as the entry point
- [x] 2.2 Regression: run `mvn package` and `java -jar target/amusement-park.jar < /dev/null` and verify BUILD SUCCESS and exit status 0
