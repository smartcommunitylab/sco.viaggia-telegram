package viaggia.command.route.train;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.ComparableId;
import mobilityservice.model.ComparableRoute;
import mobilityservice.model.ComparableRoutes;
import mobilityservice.model.MapTimeTable;
import mobilityservice.singleton.MobilityDataServicePro;
import mobilityservice.singleton.MobilityDataServiceProSingleton;
import mobilityservice.singleton.MobilityDataServiceTrento;
import mobilityservice.singleton.MobilityDataServiceTrentoSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Downloads and caches the Train information
 *
 * @author Luca Mosetti
 * @since 2017
 */
class TrainDataManagement {

    private static final Logger logger = LoggerFactory.getLogger(TrainDataManagement.class);
    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final MobilityDataServicePro service = MobilityDataServiceProSingleton.getInstance();
    private static final LoadingCache<String, ComparableRoutes> cacheTrainRoutes;
    private static final LoadingCache<ComparableId, MapTimeTable> cacheTrainTimetables;

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

        cacheTrainTimetables = CacheBuilder.newBuilder()
                .build(new CacheLoader<ComparableId, MapTimeTable>() {
                    @Override
                    public MapTimeTable load(ComparableId comparableId) throws Exception {
                        return service.getMapTimeTable(comparableId.getAgency(), comparableId.getId(), System.currentTimeMillis(), null);
                    }
                });

    }

    private static void refreshTrainTimeTable() {
        try {
            for (ComparableRoute route : getTrainsComparableRoutes()) {
                cacheTrainTimetables.refresh(route.getId());
            }
        } catch (ExecutionException e) {
            logger.error(e.getMessage());
        }
    }

    static void scheduleUpdate() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                TrainDataManagement::refreshTrainTimeTable, 0, 1, TimeUnit.HOURS
        );
    }

    static ComparableRoutes getTrainsComparableRoutes() throws ExecutionException {
        return cacheTrainRoutes.get(MobilityDataServiceTrento.TRENTO);
    }

    static MapTimeTable getTrainTimetable(ComparableId comparableId) throws ExecutionException {
        return cacheTrainTimetables.get(comparableId);
    }

}
