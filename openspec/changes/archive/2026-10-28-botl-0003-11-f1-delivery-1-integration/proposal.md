# Proposal

## Why

Milestone F1-C09 requires the Delivery 1 application to work as an integrated whole: all developed features working together, input that cannot be processed handled in a controlled manner, a from-scratch Maven build, and a resulting JAR that runs without an IDE both interactively (`java -jar target/<jar>`) and with an externally supplied command file (`java -jar target/<jar> input.txt`). The current build produces a non-executable JAR and the application can only read standard input, so the milestone cannot be demonstrated yet.

## What Changes

- Make the packaged JAR executable (`Main-Class` manifest entry) using only the standard Maven JAR plugin.
- Support the two execution modes of the JAR: no argument → interactive mode on standard input; one argument → commands read line by line from that `.txt` file, in file order.
- Handle unprocessable situations in a controlled way: missing/unreadable command file, unknown commands, and unexpected failures while executing a command are reported with an error message, never with a stack trace; processing of the remaining lines continues unless `EXIT` was reached.
- Integration tests running the currently developed commands (`HELP`, `EXIT`) through both modes and through the packaged JAR.
- Respect the Delivery 1 technical restrictions in production and test code: Java 21, standard library only, command parsing with basic `String` operations and `split`, no regular expressions.
- Commit with the exact message `[F1-C09] Complete delivery 1 integration`.

## Capabilities

### New Capabilities
- `application-execution`: How the Amusement Park application is built and launched: executable JAR, interactive mode, command-file mode and controlled handling of execution errors.

### Modified Capabilities
<!-- none -->

## Impact

- `pom.xml` (JAR manifest), `App` entry point, command processor robustness, new integration tests.
- **Flagged ambiguities / conflicts (not silently resolved):**
  - This batch applies proposals in filename order, so F1-C09 is applied before the user model, validation, creation, persistence, deletion, update (F1-C01…F1-C06), executable-JAR (F0-C02) and command-file (F1-C07) milestones. "All developed features" is interpreted as the features developed so far (`HELP`, `EXIT`); USER commands are not implemented here (later milestones). The executable JAR and command-file execution are implemented because this milestone's own statement requires them; the later F0-C02 / F1-C07 changes will verify and refine them.
  - Source §15 requires the mandatory commits to appear in roadmap order; the batch order makes `[F1-C09]` precede several earlier-numbered milestone commits. This cannot be fixed without rewriting history, which is out of scope.
  - The JAR name `nombre-aplicacion.jar` in the source is a placeholder; the Maven default `target/amusement-park-1.0-SNAPSHOT.jar` is used.
  - Behaviour with more than one command-line argument is unspecified; only the first argument is used as the command file.
