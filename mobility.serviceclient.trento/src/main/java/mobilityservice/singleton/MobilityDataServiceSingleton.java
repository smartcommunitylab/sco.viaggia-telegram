package mobilityservice.singleton;

import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;

/**
 * @author Luca Mosetti
 * @since 2017
 */
class MobilityDataServiceSingleton {

    private static final String SERVER_URL = "https://tn.smartcommunitylab.it/core.mobility";
    private static MobilityDataService instance;

    private MobilityDataServiceSingleton() {
    }

    synchronized static MobilityDataService getInstance() {
        return instance == null ? instance = new MobilityDataService(SERVER_URL) : instance;
    }

}
