package viaggia.command.start.query;

import bot.model.query.Query;

/**
 * Created by Luca Mosetti on 2017
 */
public class StartQueryParser {

    public StartQuery parse(Query query) {
        return new StartQuery(query.getMap());
    }
}
