# Tasks

## 1. USER DELETE

- [x] 1.1 Add `UserRepository.remove(User)` saving after removal; verify in `UserRepositoryTest` that the removed user is absent after reloading from the file
- [x] 1.2 Implement `USER DELETE;Email;Password` in `CommandProcessor` (format, not found, wrong password, success); verify each user-deletion scenario in `CommandProcessorTest`, and in `MainTest` that a deleted user is absent after restart

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F1-C05] Implement user deletion`; verify with `git log -1 --format=%s`
