package es.upm.parque.presentation.command;

import java.util.List;
import java.util.function.Function;

/**
 * A command whose accepted parameter counts are derived from its syntaxes and whose behavior is a function.
 */
public class SimpleCommand implements Command {

    private static final String SEPARATOR = ";";

    private final String keyword;
    private final List<String> syntaxes;
    private final String description;
    private final Function<String[], String> action;

    public SimpleCommand(String keyword, List<String> syntaxes, String description,
            Function<String[], String> action) {
        this.keyword = keyword;
        this.syntaxes = List.copyOf(syntaxes);
        this.description = description;
        this.action = action;
    }

    public SimpleCommand(String keyword, String syntax, String description, Function<String[], String> action) {
        this(keyword, List.of(syntax), description, action);
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    @Override
    public List<String> getSyntaxes() {
        return syntaxes;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean acceptsParameterCount(int count) {
        for (String syntax : syntaxes) {
            if (syntax.split(SEPARATOR, -1).length - 1 == count) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String execute(String[] parameters) {
        return action.apply(parameters);
    }
}
