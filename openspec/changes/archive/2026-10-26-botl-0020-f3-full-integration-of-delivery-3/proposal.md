# Proposal

## Why

Delivery 3 features were added change by change. The delivery closes by proving they work together with everything from E1 and E2: through the same command system, layers, controllers and repositories, in every execution mode, with complete help and a model matching the code.

## What Changes

- Add runnable command scripts per delivery (`examples/input_basico.txt`, `examples/input_fase2.txt`, `examples/input_fase3.txt`) exercising E1, E2 and E3 commands together.
- Add an end-to-end integration test that wires the application exactly as `App` does (all controllers and repositories in a temporary data directory), runs the three scripts in sequence and checks key results, then restarts and checks the state is restored.
- Add a catalog-completeness test: every command the processor accepts is listed by `HELP` and described by `HELP EXTENDED`, and no listed command is unknown to the processor.
- Verify with the packaged JAR: `mvn package` builds `target/parque.jar`; it runs interactively, from a command file and with `--fly`, in normal and temporary persistence; verify `uml/model.puml` still matches the code.
- No functional changes are expected; any integration defect found is fixed within this change.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `command-processing`: help completeness for all registered commands.
- `project-build`: the repository ships per-delivery command scripts that run successfully with the packaged JAR.

## Impact

- New `examples/` scripts, new integration and catalog tests. Production code only if a defect is found.
