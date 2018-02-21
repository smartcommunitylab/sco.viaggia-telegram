package viaggia.command.parking.general.query;

import gekoramy.telegram.bot.model.Command;
import gekoramy.telegram.bot.model.query.QueryBuilder;

/**
 * QueryBuilder for ParkingCommand in general
 * (Bike and ParkingCommand information)
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class ParkingQueryBuilder extends QueryBuilder implements ParkingRegex {

    public ParkingQueryBuilder setCommand(Command command) {
        put(COMMAND, command.getCommandIdentifier());
        return this;
    }

    public ParkingQueryBuilder setName(String name) {
        put(NAME, name.substring(0, name.length() > 9 ? 9 : name.length()));
        return this;
    }

    public ParkingQueryBuilder setAvailable(int available) {
        put(VALUE, Integer.toString(available));
        return this;
    }
}
