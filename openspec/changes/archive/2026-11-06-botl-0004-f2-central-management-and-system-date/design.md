# Design

## Context

`CommandProcessor` currently matches whole lines (`HELP`, `EXIT`). SYSTEM commands carry `;`-separated arguments, so command dispatch must split the line. Delivery restrictions (basic String operations and `split`, no regex, standard library only) still apply to the code style.

## Goals / Non-Goals

**Goals:**
- Guarantee a single central instance regardless of the caller.
- Keep money exact and dates calendar-correct.

**Non-Goals:**
- Defining how users, groups, tickets or attractions are stored in the central component (later milestones).

## Decisions

- **Singleton via a private constructor plus a static, eagerly initialized holder** (`ParkController.getInstance()`). This is thread-safe and simple. Alternative: an enum singleton. Rejected because later milestones need ordinary class features (for example injecting persistence). The design keeps the pattern choice local to this class.
- **State**: `LocalDate currentDate` (initialized with `LocalDate.now()` when the class loads, which is when execution starts) and `BigDecimal basePrice` (40.00, scale 2). `BigDecimal` avoids floating-point money errors.
- **Validation lives in the controller** (`setCurrentDate(LocalDate)` rejects null; `setBasePrice(BigDecimal)` rejects non-positive values and more than 2 decimals). Parsing lives in the command layer: `LocalDate.parse` (ISO `yyyy-MM-dd`, which rejects impossible dates such as 2027-02-30) and `new BigDecimal(text)`. Domain rules stay out of the parser.
- **Command dispatch**: split the line on `;`. The first segment (trimmed) is the command keyword (`HELP`, `EXIT`, `SYSTEM DATE`, `SYSTEM PRICE`) and the remaining segments are arguments. Unknown keywords keep the existing error.
- **Messages**: `SYSTEM DATE UPDATED: <date>`, `SYSTEM PRICE UPDATED: <price>`, and errors prefixed with `ERROR:`, like existing messages.
- **Test isolation**: a package-private `reset()` restores the startup state, used only by tests. Tests cannot otherwise get a fresh singleton.

## Risks / Trade-offs

- [A global singleton makes tests share state] → Tests set the state explicitly or call `reset()` in their setup.
- [Two-decimal limit is an interpretation of "valid monetary amount"] → Documented in the spec. Euro amounts have cents.
