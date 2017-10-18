package mobilityservice.singleton;

import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;

/**
 * Created by Luca Mosetti on 2017
 */
public class MobilityDataServiceSingleton {

    private static final String SERVER_URL = "https://tn.smartcommunitylab.it/core.mobility";
    private static MobilityDataService instance;

    private MobilityDataServiceSingleton() {
    }

    /*package*/
    synchronized static MobilityDataService getInstance() {
        return instance == null ? instance = new MobilityDataService(SERVER_URL) : instance;
    }

}
