package viaggia.command.parking.bike;

import bot.model.Command;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import viaggia.command.parking.AbstractParkingCommand;
import viaggia.command.parking.general.utils.Unit;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Luca Mosetti on 2017
 */
public class BikeCommand extends AbstractParkingCommand {

    private static final Command COMMAND_ID = new Command("bike", "bikedescription");

    public BikeCommand() {
        super(COMMAND_ID, 700, Unit.METER);
    }

    @Override
    protected List<Parking> getParkings() throws ExecutionException {
        return BikeDataManagement.getBikes();
    }

    @Override
    protected int available(Parking parking) {
        return (Integer) parking.getExtra().get("bikes");
    }

    @Override
    protected String slotsToString(Parking parking) {
        return parking.getExtra() != null && parking.getExtra().containsKey("bikes") && (Integer) parking.getExtra().get("bikes") >= 0 ?
                mBB.getMessage("bikesavailable", Integer.toString((Integer) parking.getExtra().get("bikes")), Integer.toString(parking.getSlotsTotal())) :
                mBB.getMessage("slots", Integer.toString(parking.getSlotsTotal()));
    }
}
