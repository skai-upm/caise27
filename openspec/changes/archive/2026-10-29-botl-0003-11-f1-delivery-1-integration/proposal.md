# Proposal

## Why

Milestone F1-C09 closes Delivery 1: all developed features must work together, input that cannot be processed must be handled in a controlled manner, the project must build from scratch with Maven and the resulting JAR must run without an IDE, both interactively and with an externally supplied `.txt` command file. Today `mvn package` produces a plain (non-executable) JAR, so `java -jar target/<jar>` cannot start the application, and an unexpected runtime error while processing a line would abort the whole run.

## What Changes

- Configure the Maven build so `mvn package` produces an executable JAR whose manifest declares the application entry point, runnable as `java -jar target/<jar>` (interactive) and `java -jar target/<jar> input.txt` (file).
- Give the JAR a stable file name, `target/amusement-park.jar` (the source's `nombre-aplicacion.jar` is a placeholder name).
- Parse command lines with basic `String` operations and `split` (keyword = first `;`-separated field), as the technical restrictions demand; no regular expressions.
- Harden command processing: any line that cannot be processed (unknown keyword, unexpected arguments, an unexpected runtime error) is reported with an error message and processing continues with the next line; a missing or unreadable command file is reported without a stack trace.
- Add end-to-end verification that the packaged JAR runs interactively and with a command file using only the Java standard library.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `project-build`: adds the executable-JAR requirement and the standard-library-only restriction.
- `command-interface`: adds controlled handling of lines that cannot be processed and `split`-based parsing of command fields.

## Impact

- `pom.xml` (jar plugin manifest, `finalName`), `CommandProcessor`, `App`, tests.
- No new runtime dependencies (Java standard library only; JUnit remains test-scoped).

## Ambiguities and unmet prerequisites (flagged)

This change is applied before the batch has reached the milestones it nominally builds on (F0-C02, F1-C01 … F1-C07, delivered by later proposals in this batch). Per the agreed scope, only this proposal's own text is implemented. Consequently, at the end of this change the following Delivery 1 items are **not yet satisfied** and are left to their own proposals: the user model and its validation, USER CREATE/DELETE/UPDATE, file-based persistence, and "separation of the user class, main class, and persistence class" with encapsulated attributes. The Delivery 1 example flow (`USER CREATED`, …) therefore still reports USER commands as unrecognised.

The Git-history requirement lists mandatory commit messages. In this batch each commit uses the orchestrator's subject format and carries the milestone message (`[F1-C09] Complete delivery 1 integration`) verbatim in its body; the strict ordering of the listed commits cannot be met because the batch order differs from the milestone order.
