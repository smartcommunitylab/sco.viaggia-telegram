package viaggia.command.route.general.query;

import bot.model.query.Query;

/**
 * Created by Luca Mosetti on 2017
 * <p>
 * RouteQueryParser for Routes in general
 * (Bus and Train information)
 */
public class RouteQueryParser {

    public RouteQuery parse(Query query) {
        return new RouteQuery(query.getMap());
    }
}
