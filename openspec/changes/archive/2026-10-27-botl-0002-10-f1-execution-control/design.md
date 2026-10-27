# Design

## Context

The project only contains the quickstart `App` class (see proposal.md - Why). Milestones that normally precede F1-C08 (executable JAR F0-C02, USER commands F1-C02..C06, command file input F1-C07) have not been applied yet in this repository, so F1-C08 needs the smallest command loop that makes HELP/EXIT observable from both sources.

## Goals / Non-Goals

**Goals:**
- One processing path for every command, independent of the input source.
- `HELP` and `EXIT` exactly as specified; EXIT stops all further processing.

**Non-Goals:**
- USER commands (only documented in HELP, as the source mandates).
- Executable JAR manifest configuration (F0-C02).
- Detailed file-mode guarantees beyond what EXIT needs (F1-C07 refines them: blank lines, missing/unreadable file).

## Decisions

- **`CommandProcessor`** with `boolean process(String line)` (returns `false` when the application must stop) writes to an injected `PrintStream`. Shared by both sources, satisfying "same behaviour regardless of source". Alternative (separate loops per source) rejected: it would duplicate behaviour and contradict F1-C07.
- **`CommandRunner.run(BufferedReader)`** reads lines until EOF or until `process` returns `false`. `App.main` builds the reader from `System.in` (no args) or from the file in `args[0]`.
- **Command matching:** the line is trimmed; the keyword is compared exactly (`HELP`, `EXIT`, uppercase as in the source). Blank lines are skipped so they are not reported as unknown commands (consistent with F1-C07, which will formalise it).
- **File open failure:** printed as a controlled error message and the application ends; F1-C07 owns the detailed behaviour.
- **HELP content (ambiguity, flagged):** the milestone says HELP shows "the operations currently available", while section 11 lists the USER commands as the minimum content. The source list is contractual, so HELP prints it verbatim even though USER commands are implemented by later milestones; until then they are rejected as unrecognised.
- **Mandatory commit:** `[F1-C08] Add help and exit commands` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [HELP advertises USER commands that are not yet implemented] → Documented ambiguity; resolved when the user milestones are applied.
- [Output wording is not specified by the source] → Tests check the contractual command syntax strings, not free text.
