# Tasks

## 1. Regenerate the model

- [x] 1.1 Rewrite `uml/model.puml` from the instance fields of every class in `src/main/java/es/upm/parque`, applying the attribute, naming, cross/arrow, multiplicity and usage conventions; verify `plantuml -checkonly uml/model.puml` succeeds
- [x] 1.2 Cross-check every attribute and relationship name in the model against the fields in the source code with a script; verify it reports no unknown element and every class is present
- [x] 1.3 Verify `git diff --stat -- src` is empty (no functional change) and `mvn test` still passes
