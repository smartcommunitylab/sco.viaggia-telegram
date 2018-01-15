package mobilityservice.singleton;

/**
 * Created by Luca Mosetti in 2017
 */
public class MobilityDataServiceTrentoSingleton {

    private static MobilityDataServiceTrento instance;

    private MobilityDataServiceTrentoSingleton() {
    }

    public synchronized static MobilityDataServiceTrento getInstance() {
        return instance == null ? instance = new MobilityDataServiceTrento() : instance;
    }
}
