# Tasks

## 1. Validation rules

- [x] 1.1 Create `UserDataValidator` with the DNI, email, password and card checks and the combined `validate`; verify with unit tests for every source example (valid and invalid), malformed DNIs, and a combined check naming DNI, email and card

## 2. Integration

- [x] 2.1 Apply the password rules in `ParkController.setPassword` before storing; verify with a test that a weak new password is rejected and the old one still verifies
- [x] 2.2 Update `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` (including the architecture test and no regex usage in the validator)
