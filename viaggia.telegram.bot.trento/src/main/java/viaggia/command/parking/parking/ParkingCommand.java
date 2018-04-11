package viaggia.command.parking.parking;

import gekoramy.telegram.bot.model.Command;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import viaggia.command.parking.AbsParkingCommand;
import viaggia.utils.Unit;

import java.util.List;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class ParkingCommand extends AbsParkingCommand {
    private static final Command COMMAND_ID = new Command("parking", "parking_description");

    public ParkingCommand() {
        super(COMMAND_ID, 2, Unit.KILOMETER);
    }

    @Override
    protected List<Parking> getParkingList() {
        return ParkingDataManagement.getParkingList();
    }

    @Override
    protected int available(Parking parking) {
        return parking.getSlotsAvailable();
    }

    @Override
    protected String slotsToString(Parking parking, int userId) {
        return parking.isMonitored() && parking.getSlotsAvailable() >= 0 ?
                mBB.getMessage(userId, "slots_available", Integer.toString(parking.getSlotsAvailable()), Integer.toString(parking.getSlotsTotal())) :
                mBB.getMessage(userId, "slots", Integer.toString(parking.getSlotsTotal()));
    }
}
