package viaggia.command.route.train;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
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
 * Created by Luca Mosetti on 2017
 * <p>
 * Downloads and caches the Train information
 */
/*package*/ class TrainDataManagement {

    private static final Logger logger = LoggerFactory.getLogger(TrainDataManagement.class);
    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final MobilityDataServicePro service = MobilityDataServiceProSingleton.getInstance();
    private static final Supplier<ComparableRoutes> supplierTrains;
    private static final LoadingCache<ComparableId, MapTimeTable> cacheTrainTimetables;


    static {
        supplierTrains = Suppliers.memoizeWithExpiration(() -> {
            try {
                return trento.getTrains();
            } catch (MobilityServiceException e) {
                e.printStackTrace();
            }

            return null;
        }, 1, TimeUnit.DAYS);

        cacheTrainTimetables = CacheBuilder.newBuilder()
                .build(new CacheLoader<ComparableId, MapTimeTable>() {
                    @Override
                    public MapTimeTable load(ComparableId comparableId) throws Exception {
                        return service.getMapTimeTable(comparableId.getAgency(), comparableId.getId(), System.currentTimeMillis(), null);
                    }
                });

    }

    private static void refreshTrainTimeTable(ComparableId comparableId) {
        cacheTrainTimetables.refresh(comparableId);
    }

    /*package*/
    static void scheduleUpdate() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    try {
                        for (ComparableRoute route : getTrainsComparableRoutes()) {
                            refreshTrainTimeTable(route.getId());
                        }
                    } catch (ExecutionException e) {
                        logger.error(e.getMessage());
                    }
                }, 0, 1, TimeUnit.HOURS
        );
    }

    /*package*/
    static ComparableRoutes getTrainsComparableRoutes() throws ExecutionException {
        return supplierTrains.get();
    }

    /*package*/
    static MapTimeTable getTrainTimetable(ComparableId comparableId) throws ExecutionException {
        return cacheTrainTimetables.get(comparableId);
    }

}
