package viaggia.command.language.query;

import bot.model.Command;
import bot.model.query.QueryBuilder;

/**
 * Created by Luca Mosetti on 2017
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
