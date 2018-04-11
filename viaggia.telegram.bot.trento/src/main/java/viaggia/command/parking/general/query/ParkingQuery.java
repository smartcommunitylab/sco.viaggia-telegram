package viaggia.command.parking.general.query;

import gekoramy.telegram.bot.model.query.Query;

import java.util.Map;

/**
 * Query for ParkingCommand in general
 * (Bike and ParkingCommand information)
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class ParkingQuery extends Query implements ParkingRegex {

    ParkingQuery(Map<String, String> map) {
        super(map);
    }

    public String getName() {
        return super.get(NAME);
    }

    public String getValue() {
        return super.get(VALUE);
    }
}
