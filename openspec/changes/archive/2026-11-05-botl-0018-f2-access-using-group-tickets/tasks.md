# Tasks

## 1. Group access

- [x] 1.1 Remove the group-ticket guard from `ATTRACTION ENTER`; verify tests: whole group enters, insufficient capacity admits nobody, wrong date rejected; update the previous "group not yet accepted" test accordingly

## 2. Exit and status

- [x] 2.1 Implement `ATTRACTION EXIT` and `ATTRACTION STATUS` in `AttractionCommands`, dispatched from `CommandProcessor`, and list both in `HELP`; verify tests for every exit/status scenario (frees capacity, individual group exit, invalid exits, mixed-entry status counts, empty status, unknown ids)

## 3. Integration

- [x] 3.1 End-to-end with the JAR: users, group, group ticket, individual ticket, group entry, capacity rejection, exit, status, re-entry after exit; verify responses and exit status 0; full `mvn package` regression
