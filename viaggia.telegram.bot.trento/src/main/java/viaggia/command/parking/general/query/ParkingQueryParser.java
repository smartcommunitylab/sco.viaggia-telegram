package viaggia.command.parking.general.query;

import bot.model.query.Query;

/**
 * Created by Luca Mosetti in 2017
 * <p>
 * ParkingQueryParser for ParkingCommand in general
 * (Bike and ParkingCommand information)
 */
public class ParkingQueryParser {

    public ParkingQuery parse(Query query) {
        return new ParkingQuery(query.getMap());
    }
}
