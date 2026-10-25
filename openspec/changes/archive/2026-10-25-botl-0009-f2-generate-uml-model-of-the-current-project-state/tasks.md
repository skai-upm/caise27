# Tasks

## 1. Model

- [x] 1.1 Rewrite `uml/model.puml` from the current code following the notation rules (attributes without labels, relationships named after fields with holder marks and both multiplicities, inheritance, aggregation/composition, usage only for `App`, `UserValidator`, `UserField`, no methods); verify with `plantuml -checkonly uml/model.puml`

## 2. Consistency check

- [x] 2.1 Add `UmlModelTest` checking by reflection that every relationship and attribute exists as a field of the right kind, every production class/enum appears and every class-typed field has a relationship; verify `mvn test` passes and `git diff --stat src/main` is empty
