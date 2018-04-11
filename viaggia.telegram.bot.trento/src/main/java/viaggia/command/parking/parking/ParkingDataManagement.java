package viaggia.command.parking.parking;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.ParkingList;
import mobilityservice.singleton.MobilityDataServiceTrento;
import mobilityservice.singleton.MobilityDataServiceTrentoSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Luca Mosetti
 * @since 2017
 */
class ParkingDataManagement {

    private static final Logger logger = LoggerFactory.getLogger(ParkingDataManagement.class);
    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final Supplier<ParkingList> supplierParkingList = Suppliers.memoizeWithExpiration(() -> {
        try {
            return trento.getParkings();
        } catch (MobilityServiceException e) {
            logger.error(e.getMessage());
        }
        return null;
    }, 1, TimeUnit.MINUTES);


    static ParkingList getParkingList() {
        return supplierParkingList.get();
    }
}
