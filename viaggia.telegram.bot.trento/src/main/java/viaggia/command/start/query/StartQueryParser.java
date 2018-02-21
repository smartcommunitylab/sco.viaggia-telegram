package viaggia.command.start.query;

import gekoramy.telegram.bot.model.query.Query;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class StartQueryParser {

    public static StartQuery parse(Query query) {
        return new StartQuery(query.getMap());
    }
}
