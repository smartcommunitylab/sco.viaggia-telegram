package viaggia.command.parking.general.query;

import bot.model.query.Query;

import java.util.Map;

/**
 * Created by Luca Mosetti on 2017
 * <p>
 * Query for ParkingCommand in general
 * (Bike and ParkingCommand information)
 */
public class ParkingQuery extends Query implements ParkingRegex {

    /*package*/ ParkingQuery(Map map) {
        super(map);
    }

    public String getName() {
        return super.get(NAME);
    }

    public String getValue() {
        return super.get(VALUE);
    }
}
