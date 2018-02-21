package viaggia.command.route.general.query;

import gekoramy.telegram.bot.model.query.Query;
import mobilityservice.model.ComparableId;

import java.util.Map;

/**
 * Query for Routes in general
 * (Bus and Train information)
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class RouteQuery extends Query implements RouteRegex {

    private final ComparableId id;

    RouteQuery(Map<String, String> map) {
        super(map);

        // compatibility
        id = new ComparableId(super.get(ID), super.get(AGENCY) == null ? super.get("AGENCY") : super.get(AGENCY));
    }

    public ComparableId getId() {
        return id;
    }

    public String getValue() {
        return super.get(VALUE);
    }

    public String getStopId() {
        return super.get(STOP_ID);
    }
}
