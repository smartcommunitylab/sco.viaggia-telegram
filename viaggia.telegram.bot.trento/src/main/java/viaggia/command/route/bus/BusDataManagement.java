package viaggia.command.route.bus;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.Buses;
import mobilityservice.model.ComparableRoute;
import mobilityservice.singleton.MobilityDataServiceTrento;
import mobilityservice.singleton.MobilityDataServiceTrentoSingleton;
import viaggia.command.route.general.utils.MapTimeTableManagement;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Luca Mosetti
 * @since 2017
 */
class BusDataManagement extends MapTimeTableManagement {

    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final LoadingCache<String, Buses> cacheBuses;

    static {
        cacheBuses = CacheBuilder.newBuilder()
                .refreshAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, Buses>() {
                    @Override
                    public Buses load(String key) throws Exception {
                        Buses tmp = trento.getBuses();

                        if (tmp == null)
                            throw new MobilityServiceException();

                        return tmp;
                    }
                });
    }

    BusDataManagement() {
        super(false);
    }

    Buses getBusRoutes() throws ExecutionException {
        return cacheBuses.get(MobilityDataServiceTrento.TRENTO);
    }

    @Override
    protected Set<ComparableRoute> getRoutes() throws ExecutionException {
        Set<ComparableRoute> routes = new HashSet<>();

        getBusRoutes().forEach(
                bus -> {
                    routes.add(bus.getDirect());
                    if (bus.hasReturn()) routes.add(bus.getReturn());
                }
        );

        return routes;
    }
}
