package viaggia.command.route.general.query;

import bot.model.Command;
import bot.model.query.QueryBuilder;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;

/**
 * Created by Luca Mosetti on 2017
 * <p>
 * QueryBuilder for Routes in general
 * (Bus and Train information)
 */
public class RouteQueryBuilder extends QueryBuilder implements RouteRegex {

    public RouteQueryBuilder setCommand(Command command) {
        put(COMMAND, command.getCommandIdentifier());
        return this;
    }

    public RouteQueryBuilder setId(Id id) {
        put(ID, id.getId());
        put(AGENCY, id.getAgency());
        return this;
    }

    public RouteQueryBuilder setValue(String value) {
        put(VALUE, value);
        return this;
    }

    public RouteQueryBuilder setStopId(String stopId) {
        put(STOP_ID, stopId);
        return this;
    }
}
