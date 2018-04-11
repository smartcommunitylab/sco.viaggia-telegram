package viaggia.command.language.query;

import gekoramy.telegram.bot.model.query.Query;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class LanguageQueryParser {

    public LanguageQuery parse(Query query) {
        return new LanguageQuery(query.getMap());
    }
}
