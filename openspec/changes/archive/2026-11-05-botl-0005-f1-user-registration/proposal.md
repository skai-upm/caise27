# Proposal

## Why

Milestone F1-C03 (Delivery 1, step 5) requires the application to accept the `USER CREATE` command and add new users that satisfy every previously defined restriction (F1-C02 validation) and keep DNI and email unique. Today the application has a user model and validator but no command interface, no user store, and its entry point only prints a placeholder.

## What Changes

- Introduce the text command interface (source §4): the application reads commands line by line from standard input (interactive mode) and interprets each with basic String operations and `split` on `;`, without external libraries. An unknown or malformed command prints an error and never terminates the application unexpectedly.
- Implement `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` (source §5): validate the data with the existing validation rules before storing, reject duplicate DNI or email (source §6), and report the outcome.
- Add a separate in-memory user store responsible for storing and retrieving users (source §3.2). Persistence across executions is the next milestone.
- Replace the archetype's placeholder output of the entry point with the command loop.

In scope: the command reading loop over standard input, command parsing, `USER CREATE`, uniqueness, and the user store.

Out of scope: `USER DELETE`, `USER UPDATE` (later milestones), `HELP`/`EXIT` (milestone "execution control"), file argument processing (milestone "running command sequences"), file persistence (milestone "user persistence"). Until those milestones, these commands are reported as not recognized, and standard input ending terminates the application.

## Capabilities

### New Capabilities
- `command-interface`: How text commands are read from standard input, split by `;`, dispatched, and how unknown or malformed commands are handled without terminating the application.
- `user-registration`: The `USER CREATE` command — field format, validation before storing, DNI/email uniqueness and outcome reporting.

### Modified Capabilities
- None. `executable-application` (launch with `java -jar`, exit 0 on empty input) still holds; `user-model` and `user-validation` are reused unchanged.

## Impact

- `App` becomes the command loop over standard input.
- New classes: command processor (interpretation/dispatch) and user repository (in-memory store), plus tests.
- Observable console output now consists of command responses only.
