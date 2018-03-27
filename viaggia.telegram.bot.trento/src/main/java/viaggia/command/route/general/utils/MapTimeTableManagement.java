package viaggia.command.route.general.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.ComparableId;
import mobilityservice.model.ComparableRoute;
import mobilityservice.model.ComparableStop;
import mobilityservice.model.MapTimeTable;
import mobilityservice.singleton.MobilityDataServicePro;
import mobilityservice.singleton.MobilityDataServiceProSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Luca Mosetti
 * @since 02/2018
 */
public abstract class MapTimeTableManagement {

    private final MobilityDataServicePro service = MobilityDataServiceProSingleton.getInstance();
    private final ConcurrentSkipListSet<ComparableStop> stops = new ConcurrentSkipListSet<>();
    private final ConcurrentHashMap<ComparableId, MapTimeTable> futureTimetables = new ConcurrentHashMap<>();
    private final LoadingCache<ComparableId, MapTimeTable> cacheTimetables;

    private final Logger logger;

    protected MapTimeTableManagement(boolean checkDelay) {
        logger = LoggerFactory.getLogger(getClass());
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                this::refreshTimeTable, 0, 1, TimeUnit.HOURS
        );

        cacheTimetables = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(checkDelay ?
                        new CacheLoader<ComparableId, MapTimeTable>() {
                            @Override
                            public MapTimeTable load(ComparableId comparableId) throws Exception {
                                return service.updateDelays(futureTimetables.get(comparableId));
                            }
                        } :
                        new CacheLoader<ComparableId, MapTimeTable>() {
                            @Override
                            public MapTimeTable load(ComparableId comparableId) {
                                return futureTimetables.get(comparableId);
                            }
                        }
                );
    }

    private void refreshTimeTable() {
        logger.info("starting...");
        long begin = System.currentTimeMillis();
        List<CompletableFuture> futures = new ArrayList<>();

        try {
            getRoutes().forEach(route -> futures.add(CompletableFuture.runAsync(() -> {
                try {
                    futureTimetables.put(
                            route.getId(),
                            service.getMapTimeTable(route.getId(), System.currentTimeMillis(), null)
                    );
                } catch (MobilityServiceException e) {
                    logger.error(e.getMessage(), e);
                }
            })));
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenRun(this::updateStops)
                .join();

        logger.info(System.currentTimeMillis() - begin + "ms");
    }

    private void updateStops() {
        stops.clear();
        futureTimetables.values().stream().map(MapTimeTable::getStops).forEach(stops::addAll);
    }

    public Set<ComparableStop> getStops() {
        return stops;
    }

    public List<MapTimeTable> getTimeTables() {
        return new ArrayList<>(futureTimetables.values());
    }

    public MapTimeTable getTimeTable(ComparableId comparableId) throws ExecutionException {
        return cacheTimetables.get(comparableId);
    }

    protected abstract Set<ComparableRoute> getRoutes() throws ExecutionException;
}
