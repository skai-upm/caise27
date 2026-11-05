## Context

- F3-C07 introduced `Attraction<T extends GeneralAccess>`:
  - `accept` returns the ticket typed as `T`;
  - `entryRefusal(T, PassLookup)` holds the type-specific condition, which `SpecialAttraction` uses to require an unconsumed Special Pass without consuming it.
- `AttractionManager.admit` checks type, date, refusal, already-inside and capacity, then adds the occupants.
- `Pass.consume()` exists. `PassRepository` implements `PassLookup.specialPassOf` and has `passesOf`.

## Goals / Non-Goals

**Goals:**
- `SPEED` for individual and group tickets, at the attraction types that accept SpeedPasses.
- Special Pass consumption.
- Atomicity: validate everything, then mutate.
- Output names the consumed passes.

**Non-Goals:**
- File persistence of the consumed state (F3-C09).
- Changes to refund rules, which already refuse consumed passes.

## Decisions

1. **Pass rules live in the attraction, typed on `T`.**
   - `Attraction` gets `boolean acceptsSpeedPasses()`, which is true by default; `SpecialAttraction` returns false.
   - `Attraction` gets `PassSelection passesToConsume(T ticket, boolean speed, List<IndividualTicket> entering, PassLookup passes)`. `PassSelection` is a small model record `(List<Pass> passes, String refusal)` with the factories `ok(list)` and `refused(msg)`.
   - In the base implementation:
     - when `speed` is set, it refuses if `!acceptsSpeedPasses()`;
     - otherwise, for each entering individual ticket, it picks the first unconsumed SpeedPass in creation order, refusing and naming the ticket when one has none;
     - without `speed`, it returns an empty selection.
   - `SpecialAttraction` refuses `speed` ("does not accept SpeedPasses"), and otherwise selects the unconsumed Special Pass or refuses (no pass, or already consumed).
   - The F3-C07 `entryRefusal` method is replaced by this one, so that a rule is never checked in one place and consumed in another.
2. **`PassLookup`** gains `List<Pass> passesOf(IndividualTicket ticket)`; `PassRepository` already implements it.
3. **Manager.** `enter(attractionId, ticketId, boolean speed)` returns an `Entry(List<User> entrants, List<Pass> consumed)` record in `business`. The generic `admit` runs, in order:
   1. accept (type);
   2. date;
   3. entrants, meaning holders who are not inside, with an error when there are none;
   4. capacity;
   5. `passesToConsume` for the entering tickets: `accepted.individualTickets()` filtered to those whose holder enters.

   It then consumes every selected pass and adds the entrants. Nothing changes before step 5 succeeds. The existing `enter(attractionId, ticketId)` stays as a convenience and delegates with `speed=false`.
4. **Command.**
   - `ATTRACTION ENTER` accepts 3 or 4 elements, and the 4th must be exactly `SPEED`.
   - The response is the previous text plus, when passes were consumed, `; SpeedPass consumed: SP02:1`, `; SpeedPasses consumed: SP05:1, SP06:1` or `; Special Pass consumed: PE01`.
   - The catalog syntax becomes `ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]`.
5. **UML.** It adds `PassSelection` and the `AttractionManager ..> Entry` result. The SPECIAL relationship note is updated.

## Risks / Trade-offs

- Consumed state is in memory until F3-C09 persists passes. This is documented.
