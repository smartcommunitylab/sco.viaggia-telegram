package viaggia.command.parking.bike;

import gekoramy.telegram.bot.model.Command;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import viaggia.command.parking.AbsParkingCommand;
import viaggia.utils.Unit;

import java.util.List;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class BikeCommand extends AbsParkingCommand {
    private static final Command COMMAND_ID = new Command("bike", "bike_description");

    public BikeCommand() {
        super(COMMAND_ID, 700, Unit.METER);
    }

    @Override
    protected List<Parking> getParkingList() {
        return BikeDataManagement.getBikes();
    }

    @Override
    protected int available(Parking parking) {
        return (Integer) parking.getExtra().get("bikes");
    }

    @Override
    protected String slotsToString(Parking parking, int userId) {
        return parking.getExtra() != null && parking.getExtra().containsKey("bikes") && (Integer) parking.getExtra().get("bikes") >= 0 ?
                mBB.getMessage(userId, "bikes_available", Integer.toString((Integer) parking.getExtra().get("bikes")), Integer.toString(parking.getSlotsTotal())) :
                mBB.getMessage(userId, "slots", Integer.toString(parking.getSlotsTotal()));
    }
}
