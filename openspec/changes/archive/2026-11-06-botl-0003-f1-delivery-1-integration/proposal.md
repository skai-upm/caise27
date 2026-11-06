# Proposal

## Why

Milestone F1-C09 closes Delivery 1. All features developed so far must work together. The project must build from scratch with `mvn package`, and the resulting JAR must run without an IDE, both interactively and with an external command file, and handle input it cannot process in a controlled way. Today Maven produces a plain JAR with no entry point, so `java -jar` fails. Blank lines in a command file are reported as unknown commands, and an unexpected failure while handling a line would end the application.

## What Changes

- Package the application as an executable JAR (`target/amusement-park.jar`) whose manifest names the application entry point, built by `mvn package`.
- Define interactive mode (no argument, commands from standard input) and file mode (first argument is the path of a text command file, commands run in file order) as observable behavior of the packaged application.
- Ignore blank lines in either input mode, without output.
- Make command processing failure-tolerant: an unexpected error while handling a line is reported as an error message and processing continues with the next line.
- Verify the Delivery 1 technical restrictions that apply to the current code: Java 21, standard library only at runtime, no regular expressions, frameworks, databases or encryption.
- Add an end-to-end verification that builds the JAR and runs it in both modes.

### In scope
- Integration of the features that exist at this point (`project-initialization`, `execution-control`) into a buildable, independently executable Delivery 1 application.

### Out of scope
- USER CREATE / DELETE / UPDATE, user validation and file-based user persistence. These are separate milestones in this batch and are not implemented here. HELP keeps documenting their syntax as already specified.
- Creating the other mandated Git commits of section 15. Git history is produced by the batch process, one commit per milestone change.

## Capabilities

### New Capabilities
- `application-execution`: how the packaged application is launched and fed with commands. Covers the executable JAR, interactive and file modes, blank-line handling, and controlled handling of input or failures that cannot be processed.

### Modified Capabilities
- None. `execution-control` (HELP/EXIT/unknown commands) and `project-initialization` (Maven structure, Java 21) keep their requirements unchanged.

## Impact

- `pom.xml`: jar plugin manifest (`Main-Class`) and `finalName`.
- `es.upm.park.command.CommandRunner`: blank-line skipping and failure isolation.
- Tests: runner tests, plus an end-to-end check of the packaged JAR.
- `README.md`: build and run instructions for both modes.
