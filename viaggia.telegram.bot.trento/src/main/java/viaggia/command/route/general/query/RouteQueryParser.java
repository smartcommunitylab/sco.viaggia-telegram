package viaggia.command.route.general.query;

import gekoramy.telegram.bot.model.query.Query;

/**
 * RouteQueryParser for Routes in general
 * (Bus and Train information)
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class RouteQueryParser {

    public static RouteQuery parse(Query query) {
        return new RouteQuery(query.getMap());
    }
}
