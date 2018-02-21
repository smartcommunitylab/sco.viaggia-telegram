package viaggia.command.language.query;

import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.QueryBuilder;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class LanguageQueryBuilder extends QueryBuilder implements LanguageRegex {

    public LanguageQueryBuilder setCommand(Command command) {
        put(COMMAND, command.getCommandIdentifier());
        return this;
    }

    public LanguageQueryBuilder setLanguage(String language) {
        put(LANGUAGE, language);
        return this;
    }
}
