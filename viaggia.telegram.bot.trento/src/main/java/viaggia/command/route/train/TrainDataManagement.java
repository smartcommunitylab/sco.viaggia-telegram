package viaggia.command.route.train;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.*;
import mobilityservice.singleton.MobilityDataServicePro;
import mobilityservice.singleton.MobilityDataServiceProSingleton;
import mobilityservice.singleton.MobilityDataServiceTrento;
import mobilityservice.singleton.MobilityDataServiceTrentoSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

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
    private static final ConcurrentHashMap<ComparableId, MapTimeTable> trainTimetables = new ConcurrentHashMap<>();
    private static final Set<ComparableStop> stops = new ConcurrentSkipListSet<>();
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
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<ComparableId, MapTimeTable>() {
                    @Override
                    public MapTimeTable load(ComparableId comparableId) throws Exception {
                        return service.updateDelays(trainTimetables.get(comparableId));
                    }
                });

    }

    private static void refreshTrainTimeTable() {
        long begin = System.currentTimeMillis();
        List<ComparableStop> tmp = new ArrayList<>();
        try {
            for (ComparableRoute route : getTrainsComparableRoutes()) {
                trainTimetables.put(route.getId(), service.getMapTimeTable(route.getId(), System.currentTimeMillis(), null));
                tmp.addAll(trainTimetables.get(route.getId()).getStops());
            }
        } catch (MobilityServiceException | ExecutionException e) {
            logger.error("", e);
        }
        stops.clear();
        stops.addAll(tmp);
        logger.info(System.currentTimeMillis() - begin + "ms");
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

    static Set<ComparableStop> getStops() {
        return stops;
    }

    static List<MapTimeTable> getBusTimeTables() {
        return new ArrayList<>(trainTimetables.values());
    }
}
