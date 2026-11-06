# Proposal

## Why

Milestone F1-C01 introduces the user as an independent domain object that keeps the data later features need, is properly encapsulated, and never changes its DNI after creation. A separate class must store and retrieve users, apart from the user class and the main class. Until now the system has referred to users only by email through a placeholder directory that knows no users. Groups, tickets, passes and invoices are ready to work with real users once they exist.

## What Changes

- Add the `User` domain class with first name, surname(s), DNI, email address, password, telephone number and card number. All attributes are private and accessed through methods. The DNI is set at creation and cannot be changed afterwards. The password attribute holds the stored (protected) value, never a plain-text password.
- Add a separate user registry that stores and retrieves users by email and by DNI. It provides the existing user-directory port (existence and DNI by email) to the central park management.
- Wire the registry into the park controller at application initialization, replacing the empty placeholder directory.

### In scope
- User model, encapsulation, immutable DNI, user registry (in memory) and wiring.

### Out of scope
- Validation of user data (F1-C02), USER CREATE/DELETE/UPDATE commands (F1-C03, F1-C05, F1-C06) and user persistence in files (F1-C04). These are later milestones in this batch.
- Replacing email references in groups, tickets and attractions with user object references (open item recorded in Delivery 2 integration).

## Capabilities

### New Capabilities
- `users`: the user domain concept (attributes, encapsulation, immutable DNI) and the separate component that stores and retrieves users.

### Modified Capabilities
- None.

## Impact

- New `business.model.User` and `business.controller.UserRegistry`. `AppConfiguration` injects the registry as the park controller's user directory.
- Tests, UML and README.
