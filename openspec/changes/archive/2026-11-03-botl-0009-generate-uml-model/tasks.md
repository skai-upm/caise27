# Tasks

## 1. Diagram

- [x] 1.1 Rewrite `uml/model.puml` from the declared fields of every class in `src/main/java` following the notation in design.md (no methods, attribute-named relationship lines with cross/diamond at the holder, multiplicities, inheritance, usage only for classes without incoming relationships); verify by cross-checking each model-typed field against one labelled line and running `plantuml -checkonly uml/model.puml`

## 2. Verification

- [x] 2.1 Verify no production or test code changed (`git diff --stat src` empty) and `mvn clean test` passes with the same test count as before
