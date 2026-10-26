# temporary-execution Specification

## Purpose

Defines the `--fly` temporary execution mode, which runs the application with all persistence redirected to a per-execution temporary directory that is removed on termination, leaving permanent data untouched.

## Requirements

### Requirement: Temporary persistence directory
When started with `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` named after the start timestamp of that execution (adding a numeric suffix if that name already exists), and SHALL use it as the location of every persistence file of the system.

#### Scenario: Directory created at startup
- **WHEN** the application is started with `--fly` on 2026-08-26 at 19:03:15
- **THEN** the directory `tmp/20260826_190315/` exists during the execution and holds the files written by that execution

### Requirement: Isolation from permanent data
In `--fly` mode all repositories SHALL read and write exclusively inside the temporary directory; permanent files under `data/` SHALL be neither read nor modified. The same repositories and business logic are used in both modes; only the configured directory differs.

#### Scenario: Permanent data untouched
- **WHEN** `data/users.txt` contains Ana and the application runs with `--fly` creating Pedro and deleting nothing
- **THEN** after the run `data/users.txt` still contains exactly Ana, and during the run Ana was not visible

### Requirement: Cleanup on termination
When an execution in `--fly` mode terminates — because input ends, `EXIT` is processed, the command file ends, the command file cannot be read, or the process is interrupted — the temporary directory of that execution SHALL be deleted together with all files it contains.

#### Scenario: Deleted after EXIT
- **WHEN** a `--fly` execution creates users and then processes `EXIT`
- **THEN** its `tmp/<timestamp>/` directory no longer exists after the application ends

### Requirement: Compatible with both execution modes
`--fly` SHALL work in interactive mode (`java -jar target/parque.jar --fly`) and in file mode (`java -jar target/parque.jar --fly input_fase2.txt`), where the remaining argument is the command file.

#### Scenario: Fly with a command file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** it processes that file using temporary persistence and deletes the directory at the end

### Requirement: Temporary mode covers all data
In `--fly` mode every persistent element, including personal passes and attraction types, SHALL be stored in the temporary directory and never in `data/`.

#### Scenario: Passes in fly mode
- **WHEN** a `--fly` execution buys passes and creates a SPECIAL attraction
- **THEN** `passes.txt` and `attractions.txt` are written only inside the temporary directory, which is deleted at the end
