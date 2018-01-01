package mobilityservice.singleton;

import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.MapTimeTable;

/**
 * Created by Luca Mosetti in 2017
 */
public class MobilityDataServicePro {

    private static final MobilityDataService service = MobilityDataServiceSingleton.getInstance();

    /*package*/ MobilityDataServicePro() {
    }

    public MapTimeTable getMapTimeTable(String agencyId, String routeId, Long when, String token) throws MobilityServiceException {
        return new MapTimeTable(service.getTimeTable(agencyId, routeId, when, token));
    }
}
