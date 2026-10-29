# Tasks

## 1. Integration

- [x] 1.1 Add `DeliveryThreeIntegrationTest` (combined session from file, restart, `--fly` cleanup, full HELP/HELP EXTENDED coverage); verify it passes together with the Delivery 1 and 2 integration tests
- [x] 1.2 Audit Delivery 3 restrictions by inspection (runtime dependencies, layering imports, no serialization/regex APIs in production) ; verify no findings
- [x] 1.3 Update `README.md` for Delivery 3; run `mvn clean package` and the JAR with a command file in normal and `--fly` modes from a temporary directory; verify outputs and that `--fly` leaves no data

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F3-C10] Complete delivery 3 integration` and verify the 31 roadmap commits appear in order
