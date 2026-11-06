# Design

## Context

`UserRegistry` (store), `UserValidator` (rules), `PasswordService` (BCrypt with the password policy) and the `User` model exist. Controllers with execution-wide responsibilities are singletons with injected collaborators (`ParkController`, `InvoiceController`). `CommandProcessor` receives its controllers by constructor.

## Goals / Non-Goals

**Goals:** a single place that coordinates user creation, and a command handler that only parses and delegates.
**Non-Goals:** user persistence (next milestone), deletion and update.

## Decisions

- **`business.controller.UserController`** (singleton, setter-injected `UserRegistry` and `PasswordService`). `createUser(firstName, surnames, dni, email, rawPassword, phone, card)`:
  1. `UserValidator.validateUser(...)`, which throws `InvalidInputException`.
  2. Uniqueness: `findByEmail` and `findByDni` throw `BusinessRuleException`. Both are checked before anything is stored.
  3. `passwords.protect(raw)`.
  4. `registry.save(new User(..., protectedPassword, ...))`.
  An unconfigured controller fails with `IllegalStateException`, as `InvoiceController` does.
- **Command**: the `USER CREATE` handler uses `withArgs(args, 7, …)` (trimmed fields) and returns `USER CREATED`. Errors flow through the existing `ParkException` handling.
- **`CommandProcessor`** gains a `UserController` constructor parameter. The no-argument constructor uses the singleton. `AppConfiguration` configures `UserController.getInstance()` with the registry and password service and passes it.
- **Test support**: `ParkControllerTestSupport.reset()` also configures `UserController` with a fresh registry and a BCrypt password service, and points `ParkController`'s user directory at that same registry.

## Risks / Trade-offs

- [BCrypt slows each creation (about 0.1 s)] → Acceptable. Tests create few users.
