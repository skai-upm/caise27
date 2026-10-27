# Design

## Context

`Attraction<T>.accept(ticket, passes)` checks the ticket kind and type-specific conditions (SPECIAL: an unconsumed Special Pass exists); `admit(T)` checks already-inside and capacity and lets everyone in, atomically. Passes have a `consumed` flag.

## Goals / Non-Goals

**Goals:**
- SPEED variant, SpeedPass and Special Pass consumption only after successful, indivisible entry.

**Non-Goals:**
- Persisting consumed states (F3-C09).

## Decisions

- **`AccessTicket.getIndividualTickets()`**: an individual ticket returns itself, a group ticket its composition — so SpeedPass requirements are checked the same way for both kinds.
- **`Attraction.acceptsSpeedPasses()`**: `true` by default, `false` in `SpecialAttraction`.
- **Post-admission hook `consumeOnEntry(T, PassLookup)`**: no-op by default; `SpecialAttraction` consumes the unconsumed Special Pass and returns it.
- **Order in `AttractionService.enter(attraction, ticket, speed)`** (generic helper): date → `accept` (kind + special condition) → if `speed`: `acceptsSpeedPasses` and select one unconsumed SpeedPass per individual ticket (pure, throws if any is missing) → `admit` (throws without changes on capacity/already-inside) → consume selected SpeedPasses → `consumeOnEntry`. Nothing is mutated before `admit` succeeds; nothing after it can fail.
- **SpeedPass choice:** first unconsumed SpeedPass of the ticket in purchase order.
- **`EntryResult`** record (`entered users`, `consumed passes`) returned to the presentation layer.
- **Output:** `ENTERED ATTRACTION A01: ana@correo.com` followed by one line per consumed pass: `SPEEDPASS CONSUMED: SP01:1` or `SPECIAL PASS CONSUMED: PE01`.
- **Option parsing:** the fourth element must be exactly `SPEED`.
- **Mandatory commit:** `[F3-C08] Extend attraction access and pass consumption` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Consumed state not yet persisted] → F3-C09 extends persistence to passes and their states.
