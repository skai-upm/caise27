# Design

## Context

The code base contains only `es.upm.parque.App` (entry point). Source §3.2 requires separate classes for representing a user, starting the application, and storing users; this change adds only the first of these (the store comes with registration/persistence milestones).

## Goals / Non-Goals

**Goals:**
- A plain, encapsulated `User` class with a final DNI.

**Non-Goals:**
- Validation of DNI/email/phone/card formats (milestone "valid users").
- Equality/identity semantics beyond Java defaults, the user store and persistence.

## Decisions

- **Package `es.upm.parque.model`** for domain classes, so the domain is separate from the entry point (`es.upm.parque`) and from future storage classes. Alternative: same package as `App`. Rejected because it blurs the separation that §3.2 asks for.
- **`private final String dni` set only by the constructor, with no setter**, so the DNI is immutable at compile time. The other six attributes are `private` with getters and setters, because the later modification milestone needs to change them.
- **All attributes as `String`**, including telephone and card number. Those values are identifiers, not quantities: leading zeros matter, and nobody does arithmetic on them. Alternative: numeric types. Rejected because they lose leading zeros and overflow on card numbers.
- **Constructor takes all seven attributes.** The source lists every attribute as part of a user. Which values are valid is decided by the next milestone.

## Risks / Trade-offs

- [Setters allow arbitrary values until validation exists] → The "valid users" milestone adds validation; this change deliberately does not invent rules.
- [Password stored as plain text in memory] → The source does not require hashing at this stage. A later password-protection milestone (Delivery 3) may revisit it.
