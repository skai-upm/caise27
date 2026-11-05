# Design

## Context

The code base is the bare quickstart archetype (`App` printing "Hello World!"). Lexicographic batch ordering means this milestone lands before the executable-JAR (F0-C02) and command-file (F1-C07) milestones, but its own text requires consistent behavior for both input sources. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One processing path for every input source, so later commands (USER ...) plug in once.
- `EXIT` short-circuits the loop; nothing after it is read or executed.
- Testable without spawning a JVM: the processor works on a `BufferedReader` and writes to an injected `PrintStream`.

**Non-Goals:**
- JAR packaging/manifest, USER commands, persistence, rich file-error policies.

## Decisions

- **`CommandProcessor` with `boolean execute(String line)` + `void run(BufferedReader)`**: `execute` returns `false` when the session must stop; `run` loops until EXIT or end of input. Alternative (a `System.exit` inside EXIT) rejected: it kills the JVM in tests and skips any future cleanup (e.g. persistence).
- **Dispatch by command keyword** (first token before `;`, or full line for keyword-only commands) through a small map of handlers so later milestones add entries without touching the loop. Exact, case-sensitive keywords, matching the source syntax.
- **`App.main`**: no args → `System.in`; first arg → open that file with UTF-8; failure to open prints a controlled error message and exits normally (no stack trace).
- **Output**: plain text to the injected stream; errors are prefixed with `ERROR:`.

## Risks / Trade-offs

- [HELP lists USER commands that do not exist yet] → mandated by the source; unknown-command handling covers their invocation until implemented.
- [Interactive prompt text would make stdin and file output differ] → no prompt is printed, keeping outputs identical.
