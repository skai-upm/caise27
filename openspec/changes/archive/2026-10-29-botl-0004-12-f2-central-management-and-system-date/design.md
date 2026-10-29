# Design

## Context

`CommandProcessor` dispatches on the first `;` field; there is no domain state yet. See proposal.md for flagged scope limits.

## Goals / Non-Goals

**Goals:** a unique central controller holding date and price; SYSTEM DATE / SYSTEM PRICE with validation.

**Non-Goals:** users, groups, tickets, attractions and any price effect on tickets.

## Decisions

- **`ParkController` singleton** with a private constructor and the initialization-on-demand holder idiom (`getInstance()`), which is thread-safe and lazy, so uniqueness holds wherever it is accessed. Alternative: enum singleton — equally valid but less natural when later milestones need to inject collaborators; static class — rejected, the source asks for an object.
- **Clock**: `LocalDate.now()` at construction time; the date is then only changed via `setCurrentDate`.
- **Price as `BigDecimal`** with scale 2 (`40.00`), avoiding floating-point error for money. Validation without regular expressions: check every character is a digit or a single `.`, at most two digits after it, at least one digit, then `compareTo(ZERO) > 0`.
- **Date parsing**: `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)` rejects impossible dates such as 2027-02-30.
- **Dispatch**: `CommandProcessor` gains cases `"SYSTEM DATE"` and `"SYSTEM PRICE"` requiring exactly two fields; it calls the controller and prints `SYSTEM DATE UPDATED` / `SYSTEM PRICE UPDATED` or an `Error: ...` message.
- **Test isolation**: a package-private `reset()` restores date and price defaults on the same instance (it does not create a new one), used by tests only.

## Risks / Trade-offs

- [Singleton state shared across tests] → tests call `reset()` in `@BeforeEach`.
- [Price format stricter than some users might expect] → documented as a flagged ambiguity.
