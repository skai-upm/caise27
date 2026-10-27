# Tasks

## 1. Group access and exit

- [x] 1.1 Allow group tickets in `AttractionService.enter` (atomic) and add `AttractionService.exit`; verify with `AttractionServiceTest`: group enters together, insufficient capacity lets nobody in, other date rejected, individual exit of a group member, user not inside, unknown attraction

## 2. Commands

- [x] 2.1 Dispatch `ATTRACTION EXIT` / `ATTRACTION STATUS` in `CommandProcessor` with HELP lines; verify with `CommandProcessorTest` status content after mixed entries and exit, error cases, and run `mvn package` successfully
