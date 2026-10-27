# Tasks

## 1. Modifiable fields

- [x] 1.1 Create `model/UserField` enum with `fromText`; verify `UserFieldTest` (six fields recognised, `DNI`/unknown return null)

## 2. USER UPDATE

- [x] 2.1 Add `UserRepository.update(User)` persisting the change; verify in `UserRepositoryTest` that a reloaded repository sees the change
- [x] 2.2 Implement USER UPDATE in `CommandProcessor` (format, field, identification, validation, email uniqueness, apply, persist); verify every user-modification scenario in `CommandProcessorTest` and the restart scenario in `MainTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F1-C06] Implement user update`; verify with `git log -1 --format=%s`
