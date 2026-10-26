package es.upm.parque;

/**
 * Program arguments: the {@code --fly} temporary-persistence flag and the optional command file.
 *
 * @param fly       whether persistence uses a temporary directory for this run
 * @param inputFile path of the command file, or {@code null} for interactive mode
 */
public record ExecutionOptions(boolean fly, String inputFile) {

    public static final String FLY_OPTION = "--fly";

    public static ExecutionOptions parse(String[] args) {
        boolean fly = false;
        String inputFile = null;
        for (String arg : args) {
            if (FLY_OPTION.equals(arg)) {
                fly = true;
            } else if (inputFile == null) {
                inputFile = arg;
            }
        }
        return new ExecutionOptions(fly, inputFile);
    }
}
