# Tasks

## 1. Diagram

- [x] 1.1 Rewrite `uml/model.puml` from the current sources following the notation rules (no methods, primitive/enum attributes only without technical labels, labels = field names, holder marked with cross or diamond, inferable multiplicities, usage arrows only for classes without incoming relationships); verify `plantuml -checkonly uml/model.puml` succeeds

## 2. Verification

- [x] 2.1 Extend `UmlModelTest` to check by reflection that every relationship label is a field of the holder class typed as the target (or a list of it), that every production class/enum is declared, and that class bodies contain no methods or technical labels; verify `mvn test` passes with production sources unchanged (`git diff --stat src/main` empty)
