# Tasks

## 1. Model

- [x] 1.1 Rewrite `uml/model.puml` from the current field inventory following the conventions in the design; verify with `plantuml -checkonly`
- [x] 1.2 Cross-check every class, attribute and relationship label against the source fields and confirm nothing is invented or missing; verify with a script listing reference fields vs. diagram labels

## 2. No behavior change

- [x] 2.1 Verify `git diff --stat -- src` is empty and `mvn test` passes
