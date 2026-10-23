package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.exception.ValidationException;

/** Argument-count checks for command handlers. */
public final class Args {

    private Args() {
    }

    /** Requires one of the accepted argument counts; the message shows the usage. */
    public static void require(String[] args, String usage, int... accepted) throws ValidationException {
        StringBuilder expected = new StringBuilder();
        for (int count : accepted) {
            if (args.length == count) {
                return;
            }
            if (expected.length() > 0) {
                expected.append(" or ");
            }
            expected.append(count);
        }
        throw new ValidationException("Wrong number of arguments (" + args.length + ", expected " + expected
                + "). Usage: " + usage);
    }

    /** The argument at {@code index}, or null when absent. */
    public static String optional(String[] args, int index) {
        return args.length > index ? args[index] : null;
    }
}
