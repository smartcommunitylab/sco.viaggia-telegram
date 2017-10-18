package viaggia.command.parking.parking;

import bot.model.Command;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import viaggia.command.parking.AbstractParkingCommand;
import viaggia.command.parking.general.utils.Unit;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Luca Mosetti on 2017
 */
public class ParkingCommand extends AbstractParkingCommand {

    private static final Command COMMAND_ID = new Command("parking", "parkingdescription");

    public ParkingCommand() {
        super(COMMAND_ID, 2, Unit.KILOMETER);
    }

    @Override
    protected List<Parking> getParkings() throws ExecutionException {
        return ParkingDataManagement.getParkings();
    }

    @Override
    protected int available(Parking parking) {
        return parking.getSlotsAvailable();
    }

    @Override
    protected String slotsToString(Parking parking) {
        return parking.isMonitored() && parking.getSlotsAvailable() >= 0 ?
                mBB.getMessage("slotsavailable", Integer.toString(parking.getSlotsAvailable()), Integer.toString(parking.getSlotsTotal())) :
                mBB.getMessage("slots", Integer.toString(parking.getSlotsTotal()));
    }
}
