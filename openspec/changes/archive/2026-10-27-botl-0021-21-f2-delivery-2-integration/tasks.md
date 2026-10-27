# Tasks

## 1. Integration

- [x] 1.1 Rewrite HELP with the Delivery 2 syntax list and update affected tests; verify every listed line appears in `CommandProcessorTest`
- [x] 1.2 Add `Delivery2IntegrationTest` (mixed command file, normal vs `--fly`) and relationship-direction checks in `TechnicalRestrictionsTest`; verify they pass
- [x] 1.3 Update `README.md` (Delivery 2 commands, `--fly`, data files); verify `mvn clean package` and a manual `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt`

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F2-C10] Complete delivery 2 integration`; verify with `git log -1 --format=%s`
