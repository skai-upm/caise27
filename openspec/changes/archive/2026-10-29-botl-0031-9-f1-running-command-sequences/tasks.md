# Tasks

## 1. Verification

- [x] 1.1 Add `CommandFileTest`: source sequence from a file with blank lines vs. the same commands on stdin (identical output, users created and updated), directory as command file (controlled error), interactive input without EXIT (all commands processed); verify it passes, fixing any defect found
- [x] 1.2 Run `mvn clean package` and verify `BUILD SUCCESS`; run `java -jar <abs>/target/amusement-park.jar input.txt` with the source example file and with a directory argument
