# Design

## Context

`UserController` creates users into an in-memory `UserRegistry`. Park state persistence follows a port/adapter pattern: `ParkStorage` in business, `TextParkStorage` plus repositories in persistence, built by `AppConfiguration` for the data directory (`data/` or `tmp/<stamp>`). `TextFile` provides tolerant line parsing and atomic writes.

## Goals / Non-Goals

**Goals:** user persistence in the mandated line format, through the same layered mechanism.
**Non-Goals:** encryption beyond BCrypt, migration.

## Decisions

- **Port `business.controller.UserStorage`** with `List<User> load()` and `void save(List<User> users)`, and a `NONE` default.
- **Adapter `persistence.UserRepository implements UserStorage`** (`users.txt` via `StoragePaths.users()`). It uses `TextFile.parseAll` with exactly 7 fields, building the `User` directly from the stored (already protected) password, and `TextFile.writeLines` in the field order of the source.
- **Saving**: `UserController.configure(registry, passwords, storage)`. After every successful mutation (create now, and delete/update later) it calls `storage.save(registry.all())`. The registry stays a pure in-memory store.
- **Loading**: `App` calls `configured.users().restore(configured.userStorage().load())` before running commands, in the same `try` as the park restore, so a storage error ends startup in a controlled way.
- **`UserRegistry.restore(List<User>)`** replaces the content.

## Risks / Trade-offs

- [Users loaded from older plain-text files would carry plain passwords] → Migration is explicitly not required. Such users will fail BCrypt verification.
