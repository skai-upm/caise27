# Tasks

## 1. Generic attractions

- [x] 1.1 Add access interfaces, make tickets implement them, and turn `Attraction` into `Attraction<T extends AccessTicket>` with the four subclasses, `AttractionType` and `PassLookup`; verify unit tests per type for accepted/rejected tickets and the Special Pass condition
- [x] 1.2 Adapt `AttractionService` (typed creation, entry through the attraction, injected `PassLookup`), the processor (`ATTRACTION CREATE` with optional type, type in STATUS) and `AttractionFileStorage` (type column, default GENERAL); verify processor and storage tests including an old 3-column file
- [x] 1.3 Update `uml/model.puml` and the consistency test for generic declarations; run `mvn package` and a JAR session creating the four types and trying accepted/rejected entries; verify responses

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F3-C07] Add generic attraction access types`; verify with `git log -1 --format=%s`
