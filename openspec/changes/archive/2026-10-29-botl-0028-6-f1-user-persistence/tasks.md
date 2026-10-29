# Tasks

## 1. User file format

- [x] 1.1 Write and read users as `FirstName;Surnames;DNI;Email;Password;Phone;Card` (protected password), keeping earlier layouts readable; verify with tests for the exact line format, restart of users created with USER CREATE (Ana and Pedro from the source example), and reading an earlier email-first line
- [x] 1.2 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR twice in normal mode (create users, restart, buy a ticket for a stored user) and inspect `data/users.txt`
