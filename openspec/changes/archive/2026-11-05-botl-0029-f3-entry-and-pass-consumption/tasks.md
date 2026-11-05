## 1. Model rules

- [x] 1.1 Add the `PassSelection` record, `PassLookup.passesOf`, and `Attraction.acceptsSpeedPasses()`. Add `Attraction.passesToConsume(T, speed, entering, PassLookup)`, which replaces `entryRefusal`: in the base class, one unconsumed SpeedPass per entering ticket when `speed` is set.
- [x] 1.2 `SpecialAttraction`: refuse `SPEED`; select its unconsumed Special Pass; refuse when it is missing or consumed.

## 2. Entry

- [x] 2.1 `AttractionManager.enter(attraction, ticket, speed)` returns `Entry(entrants, consumed)`. It checks everything first (type, date, entrants, capacity, passes), then consumes the selected passes and admits the entrants.
- [x] 2.2 `AttractionCommands`:
  - optional `SPEED` element, with other values rejected;
  - the response names the consumed passes;
  - the catalog syntax becomes `[;SPEED]`.

## 3. Documentation and verification

- [x] 3.1 Update the UML (`PassSelection`, `Entry`) and README.
- [x] 3.2 Add tests:
  - individual SPEED consumes exactly one pass;
  - no SpeedPass available;
  - a failed entry (full or wrong date) keeps the passes;
  - normal entry consumes nothing;
  - group SPEED is atomic: success, one member without a pass, lack of capacity;
  - SPEED rejected at SPECIAL and for a group at INDIVIDUAL;
  - Special Pass consumed on entry and then refused; a failed special entry keeps the pass;
  - invalid option;
  - consumed passes cannot be refunded and appear as consumed on the invoice.
- [x] 3.3 Run `mvn package` and an end-to-end JAR regression: identical to the baseline except for the HELP syntax line.
