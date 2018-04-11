package viaggia.command.language.query;

import gekoramy.telegram.bot.model.query.Query;

import java.util.Map;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class LanguageQuery extends Query implements LanguageRegex {

    LanguageQuery(Map<String, String> map) {
        super(map);
    }

    public String getLanguage() {
        return super.get(LANGUAGE);
    }
}
