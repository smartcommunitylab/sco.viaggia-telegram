package mobilityservice.singleton;

/**
 * Created by Luca Mosetti on 2017
 */
public class MobilityDataServiceProSingleton {

    private static MobilityDataServicePro instance;

    private MobilityDataServiceProSingleton() {
    }

    public synchronized static MobilityDataServicePro getInstance() {
        return instance == null ? instance = new MobilityDataServicePro() : instance;
    }
}
