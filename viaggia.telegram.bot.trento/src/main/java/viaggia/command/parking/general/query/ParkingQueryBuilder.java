package viaggia.command.parking.general.query;

import bot.model.Command;
import bot.model.query.QueryBuilder;

/**
 * Created by Luca Mosetti on 2017
 * <p>
 * QueryBuilder for ParkingCommand in general
 * (Bike and ParkingCommand information)
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
