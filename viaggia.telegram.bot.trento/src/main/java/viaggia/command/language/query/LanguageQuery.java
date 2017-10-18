package viaggia.command.language.query;

import bot.model.query.Query;

import java.util.Map;

/**
 * Created by Luca Mosetti on 2017
 */
public class LanguageQuery extends Query implements LanguageRegex {

    /*package*/ LanguageQuery(Map map) {
        super(map);
    }

    public String getLanguage() {
        return super.get(LANGUAGE);
    }
}