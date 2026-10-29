# Proposal

## Why

Milestone F1-C08 requires the general `HELP` and `EXIT` commands: HELP shows the operations currently available and EXIT terminates the application in a controlled manner, consistently whether commands come from standard input or from a file, with EXIT preventing any subsequent command from executing. The project currently is only the quickstart skeleton, so it has no command processing at all.

## What Changes

- Introduce a single line-based command-processing component used for every input source (standard input and a command file), so command behavior cannot diverge between sources.
- Add the `HELP` command, which prints the available commands and their basic syntax, covering at least: `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP`, `EXIT`.
- Add the `EXIT` command, which stops processing: no later line of the current input source is executed, and the application terminates normally.
- Wire the application entry point so that it reads commands from standard input when started without arguments and from the file given as first argument otherwise — only the minimum needed to make the source-independent behavior of HELP/EXIT observable. Full command-file handling rules belong to milestone F1-C07.
- Lines that are not a recognised command are reported with an error message and processing continues (USER commands are listed by HELP but are implemented by their own milestones).

## Capabilities

### New Capabilities
- `command-interface`: How the application receives text commands (input sources, line processing) and the general commands HELP and EXIT.

### Modified Capabilities
- None.

## Impact

- New production classes in `es.upm.amusementpark` (command processor, entry point update in `App`).
- New JUnit tests for HELP, EXIT and the input sources.
- No new dependencies. `mvn package` must keep passing.

## Ambiguities flagged

- The source does not define the exact HELP output layout, the message for an unrecognised command, or case sensitivity of keywords. This change prints one command syntax per line, uses an explanatory error line for unknown commands, and matches keywords exactly as written (upper case) after trimming surrounding whitespace.
- USER commands appear in HELP (as the source requires) although their behavior is delivered by other milestones; until then they are reported as not recognised.
