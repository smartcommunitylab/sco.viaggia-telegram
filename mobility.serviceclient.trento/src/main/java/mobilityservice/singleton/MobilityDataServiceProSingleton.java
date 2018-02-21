package mobilityservice.singleton;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class MobilityDataServiceProSingleton {

    private static MobilityDataServicePro instance;

    private MobilityDataServiceProSingleton() {
    }

    public synchronized static MobilityDataServicePro getInstance() {
        return instance == null ? instance = new MobilityDataServicePro() : instance;
    }
}
