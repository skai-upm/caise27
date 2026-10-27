# Tasks

## 1. Integration verification

- [x] 1.1 Add `Delivery3IntegrationTest` (complete session with rejections, normal vs `--fly` identical output and no leftover data, restart keeps consumed passes) and a HELP completeness test for all deliveries; verify with `mvn package`
- [x] 1.2 Clone the repository into a scratch directory, build with `mvn package`, run `input_fase3.txt` normally and with `--fly`; verify the mandatory Delivery 3 commits are present in order with `git log`
