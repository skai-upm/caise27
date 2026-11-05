## 1. Model

- [x] 1.1 Add the `GeneralAccess`, `IndividualAccess` and `GroupAccess` interfaces, implemented by `Ticket`, `IndividualTicket` and `GroupTicket`.
- [x] 1.2 Make `Attraction` an abstract generic `Attraction<T extends GeneralAccess>`:
  - `accept` and `entryRefusal`;
  - the subclasses `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction` and `SpecialAttraction` (Special Pass condition);
  - the `AttractionType` enum;
  - the `PassLookup` interface, implemented by `PassRepository`.

## 2. Business, command and persistence

- [x] 2.1 `AttractionManager`: creation with an optional type; entry through the generic `admit`, with the type acceptance check and the type's entry condition.
- [x] 2.2 `AttractionCommands` and `CommandCatalog`:
  - optional `<type>` in `ATTRACTION CREATE`;
  - the type named in the response.
- [x] 2.3 `ParkFileStore`: save the type, and load 3-field lines as GENERAL. `AttractionRepository` stores `Attraction<?>`.

## 3. Documentation and verification

- [x] 3.1 Update the UML: interfaces, realizations, the generic attraction with bound subclasses, `AttractionType` and `PassLookup`.
- [x] 3.2 Update README.
- [x] 3.3 Add tests:
  - creation of each type, compatible syntax and unknown type;
  - acceptance matrix for the four types with individual and group tickets;
  - Special Pass condition: none, unconsumed, consumed;
  - generics: `accept` returns typed tickets only for the bound capability, and the capability interfaces are implemented;
  - persistence of the type and loading of typeless lines.
- [x] 3.4 Run `mvn package` and an end-to-end JAR regression: identical to the baseline except for the create responses and the HELP syntax line.
