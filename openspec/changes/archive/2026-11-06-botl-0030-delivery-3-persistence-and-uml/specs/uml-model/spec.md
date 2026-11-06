# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 design represented
The diagram SHALL also show the ticket access interfaces and their implementations, the generic attraction hierarchy and attraction types, the passes (abstract pass, SpeedPass, Special Pass) with their association to the individual ticket and the cardinalities (any number of SpeedPasses and at most one Special Pass per individual ticket), the invoice Builder abstraction with its concrete builder, product parts and director, the relationships created by dependency injection (repositories into controllers, password hasher into the user controller, help formatter into the help generator), and the controllers whose uniqueness is part of the design marked as singletons.

#### Scenario: Delivery 3 elements present and consistent
- **WHEN** the diagram is checked by the consistency test
- **THEN** `Pass`, `SpeedPass`, `SpecialPass`, `InvoiceBuilder`, `TextInvoiceBuilder`, `Invoice`, `InvoiceService`, `PasswordHasher`, `BCryptPasswordHasher`, `HelpFormatter`, `HelpGenerator` and `PassService` are declared with the same kind as in the code, and their inheritance, realizations and associations match the code
