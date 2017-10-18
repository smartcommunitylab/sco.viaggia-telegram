package viaggia.command.route.general.query;

import bot.model.query.Query;
import mobilityservice.model.ComparableId;

import java.util.Map;

/**
 * Created by Luca Mosetti on 2017
 * <p>
 * Query for Routes in general
 * (Bus and Train information)
 */
public class RouteQuery extends Query implements RouteRegex {

    private final ComparableId id;

    /*package*/ RouteQuery(Map map) {
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
