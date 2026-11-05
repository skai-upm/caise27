# Design

## Context

Existing: `App` → `InteractiveSession` (reads lines), `model.User`, `model.UserValidator`. The assignment requires separate classes to represent a user, start the application, and store/retrieve users; patterns are not required in this delivery.

## Goals / Non-Goals

**Goals:** a single command-processing path reusable by the future file input (F1-C07); user storage isolated in its own class so persistence (F1-C04) can be added there.

**Non-Goals:** file persistence, other commands, HELP/EXIT.

## Decisions

- **Layers by responsibility** (dependency direction `App → InteractiveSession → CommandProcessor → UserService → UserRepository → User`):
  - `command.CommandProcessor`: splits a line with `split(";", -1)`, trims elements, dispatches on the first element, returns the response text. Knows no I/O streams.
  - `service.UserService`: validates with `UserValidator`, checks uniqueness via the repository, creates the `User`. Signals rejections with `UserOperationException` (message used for the `ERROR: ...` response).
  - `repository.UserRepository`: in-memory store (insertion-ordered) with lookup by DNI/email.
- **Responses returned as strings** rather than printed inside the processor: the session decides where to print; this keeps commands identical regardless of input source.
- **Element count must match exactly** (8 for `USER CREATE`); otherwise `ERROR: wrong number of arguments`.
- **Assumption**: duplicate checks compare DNI and email exactly as typed (the source does not define case-insensitivity); command keywords are case-sensitive uppercase as in the assignment.

## Risks / Trade-offs

- [Users are lost at exit] → expected until F1-C04 adds persistence behind `UserRepository`.
