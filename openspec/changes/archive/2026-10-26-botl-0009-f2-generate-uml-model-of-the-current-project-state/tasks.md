# Tasks

## 1. Analysis

- [x] 1.1 List every type in `src/main/java/es/upm/parque` with its non-static fields, supertypes and implemented interfaces; verify the list covers all 19 source files

## 2. Model

- [x] 2.1 Move the diagram to `uml/model.puml` and rewrite it following the derivation and notation rules (no methods, value attributes only, field-named relationships with cross/diamond, multiplicities, usage only for types without incoming relationships); verify `plantuml -checkonly uml/model.puml` passes
- [x] 2.2 Cross-check the diagram against the code: every type present, every reference field drawn as a relationship with the field's name, no relationship without a backing field, no methods or `{...}` labels; verify with a script comparing field names to relationship labels, and confirm `git diff --stat src` is empty and `mvn test` still passes
