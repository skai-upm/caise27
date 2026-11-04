# Tasks

## 1. Model regeneration

- [x] 1.1 Rewrite `uml/model.puml` from the current sources following the notation rules (attributes only, no methods, holder-side crosses/diamonds, attribute-named relationships, multiplicities, usage only for classes without incoming relationships); verify it parses with PlantUML `-checkonly`
- [x] 1.2 Cross-check the diagram against the code with a script: every class in the diagram exists in `src/main/java`, every production type appears, every relationship label is a field of its holder class, and no method signature appears; verify the script reports no mismatches

## 2. No behavior change

- [x] 2.1 Verify `git diff` shows no changes under `src/` and `mvn test` passes with the same test count as before the change
