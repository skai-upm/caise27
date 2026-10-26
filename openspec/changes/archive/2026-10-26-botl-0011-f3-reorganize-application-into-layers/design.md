# Design

## Context

20 classes live in `es.upm.parque` (see proposal). `App` already acts as composition root: it creates repositories with file paths (normal or `--fly`) and injects them into the managers, which `CommandProcessor` uses. Business classes already avoid file I/O except the repositories and `TemporaryDataDirectory`.

## Goals / Non-Goals

**Goals:** physical layering with no behaviour change; names that match the layer vocabulary (controllers).

**Non-Goals:** Singleton/dependency-injection changes and repository interfaces (next change), new features.

## Decisions

- **Package map** as listed in the proposal. `SystemDate` is business model state; `TemporaryDataDirectory` is file-system infrastructure, so persistence.
- **Rename `*Manager` → `*Controller`** (and fields/variables `xxxManager` → `xxxController`) because the business layer is described as model + rules + controllers; next deliveries refer to "controller classes". Alternative: keep names — rejected to avoid a vocabulary mismatch with the following changes.
- **`App` stays the composition root in presentation**: it is the start-up class and the only presentation class allowed to know repositories, to create them with the right paths. `CommandProcessor` depends only on business classes.
- **Visibility**: members used across the new packages become `public` (e.g. `TicketController.DEFAULT_PRICE`, `TicketRepository.DELETED_USER_NAME`); `App.run` stays package-private and its test moves to the presentation test package.
- **Mechanics**: `git mv` to keep history, scripted package/import rewrite, explicit single-type imports (no wildcards).
- **`ArchitectureTest`** scans `src/main/java`: package declarations match the layer directories; `CommandProcessor` imports nothing from `persistence` nor `java.io`/`java.nio.file`; `business.model` classes import no `java.io`/`java.nio.file`.

## Risks / Trade-offs

- [Large mechanical diff] → full existing suite (112 tests) must pass unchanged in behaviour.
- [Business ↔ persistence package cycle (controllers use repositories, repositories build model objects)] → accepted for now; repository abstractions come with dependency injection in the next change.
