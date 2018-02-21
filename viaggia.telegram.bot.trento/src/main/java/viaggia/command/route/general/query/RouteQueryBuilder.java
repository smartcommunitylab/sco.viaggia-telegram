package viaggia.command.route.general.query;

import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.QueryBuilder;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;

/**
 * QueryBuilder for Routes in general
 * (Bus and Train information)
 *
 * @author Luca Mosetti
 * @since 2017
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
