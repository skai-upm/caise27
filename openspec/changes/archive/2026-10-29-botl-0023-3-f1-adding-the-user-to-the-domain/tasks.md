# Tasks

## 1. User model

- [x] 1.1 Extend `User` with all attributes (private, DNI final, setters for the others, full and partial constructors, equality by DNI); verify with unit tests reading back every attribute, reflection checks (all private, DNI final, no DNI setter) and a phone change keeping the DNI

## 2. Persistence and separation

- [x] 2.1 Store all attributes in the users file (`email;dni;hash;firstName;surnames;phone;card`, legacy lines accepted); verify with a restart test that all seven attributes come back and the password verifies
- [x] 2.2 Add architecture assertions for the separation (no I/O in `User`, no file writing in `App`); update `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS`
