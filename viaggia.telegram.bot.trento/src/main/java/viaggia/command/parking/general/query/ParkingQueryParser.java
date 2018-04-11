package viaggia.command.parking.general.query;

import gekoramy.telegram.bot.model.query.Query;

/**
 * ParkingQueryParser for ParkingCommand in general
 * (Bike and ParkingCommand information)
 *
 * @author Luca Mosetti
 * @since 2017
 */
public class ParkingQueryParser {

    public static ParkingQuery parse(Query query) {
        return new ParkingQuery(query.getMap());
    }
}
