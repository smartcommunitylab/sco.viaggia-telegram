package viaggia.command.language.query;

import bot.model.query.Query;

/**
 * Created by Luca Mosetti on 2017
 */
public class LanguageQueryParser {

    public LanguageQuery parse(Query query) {
        return new LanguageQuery(query.getMap());
    }
}
