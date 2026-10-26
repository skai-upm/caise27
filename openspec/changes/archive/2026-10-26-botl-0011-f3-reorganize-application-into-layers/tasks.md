# Tasks

## 1. Baseline

- [x] 1.1 Record the output of a command script covering Delivery 1 and 2 commands with the current jar; verify the output file is saved for comparison

## 2. Reorganization

- [x] 2.1 Move main classes into the layer packages with the renames from the design and explicit imports; verify `mvn compile` succeeds and no class remains in the old flat package except `App`
- [x] 2.2 Move tests into the matching packages with the same renames; verify `mvn test` passes with the same number of tests (114)
- [x] 2.3 Verify the presentation package does not reference repositories or file APIs for data and the model package does not reference persistence (grep checks)

## 3. Verification and model

- [x] 3.1 Run `mvn package` and the baseline script with the new jar; verify the output is identical
- [x] 3.2 Update `uml/model.puml` with the new class names and packages; verify with `plantuml -checkonly`
