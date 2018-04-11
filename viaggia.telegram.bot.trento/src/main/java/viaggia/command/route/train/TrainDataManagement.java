package viaggia.command.route.train;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.ComparableRoute;
import mobilityservice.model.ComparableRoutes;
import mobilityservice.singleton.MobilityDataServiceTrento;
import mobilityservice.singleton.MobilityDataServiceTrentoSingleton;
import viaggia.command.route.general.utils.MapTimeTableManagement;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Downloads and caches the Train information
 *
 * @author Luca Mosetti
 * @since 2017
 */
class TrainDataManagement extends MapTimeTableManagement {

    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final LoadingCache<String, ComparableRoutes> cacheTrainRoutes;

    static {
        cacheTrainRoutes = CacheBuilder.newBuilder()
                .refreshAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, ComparableRoutes>() {
                    @Override
                    public ComparableRoutes load(String agencyId) throws Exception {
                        ComparableRoutes tmp = trento.getTrains();

                        if (tmp == null)
                            throw new MobilityServiceException();

                        return tmp;
                    }
                });
    }

    TrainDataManagement() {
        super(true);
    }

    ComparableRoutes getTrainsComparableRoutes() throws ExecutionException {
        return cacheTrainRoutes.get(MobilityDataServiceTrento.TRENTO);
    }

    @Override
    protected Set<ComparableRoute> getRoutes() throws ExecutionException {
        return new HashSet<>(getTrainsComparableRoutes());
    }
}
