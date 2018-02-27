package viaggia.command.route.bus;

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
 * @author Luca Mosetti
 * @since 2017
 */
class BusDataManagement {

    private static final Logger logger = LoggerFactory.getLogger(BusDataManagement.class);
    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final MobilityDataServicePro service = MobilityDataServiceProSingleton.getInstance();
    private static final LoadingCache<String, Buses> cacheBuses;
    private static final ConcurrentHashMap<ComparableId, MapTimeTable> busTimetables = new ConcurrentHashMap<>();
    private static final ConcurrentSkipListSet<ComparableStop> stops = new ConcurrentSkipListSet<>();
    private static final LoadingCache<ComparableId, MapTimeTable> cacheBusTimetables;

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

        // optimisation: by now, there are no delay info for buses
        cacheBusTimetables = CacheBuilder.newBuilder()
                //.expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<ComparableId, MapTimeTable>() {
                    @Override
                    public MapTimeTable load(ComparableId comparableId) throws Exception {
                        //return service.updateDelays(busTimetables.get(comparableId));
                        return busTimetables.get(comparableId);
                    }
                });
    }

    private static void refreshBusTimeTable() {
        long begin = System.currentTimeMillis();
        List<ComparableStop> tmp = new ArrayList<>();
        try {
            for (Bus bus : getBusRoutes()) {
                if (bus != null && bus.getDirect() != null) {
                    busTimetables.put(bus.getDirect().getId(), service.getMapTimeTable(bus.getDirect().getId(), System.currentTimeMillis(), null));
                    tmp.addAll(busTimetables.get(bus.getDirect().getId()).getStops());

                    if (bus.hasReturn()) {
                        busTimetables.put(bus.getReturn().getId(), service.getMapTimeTable(bus.getReturn().getId(), System.currentTimeMillis(), null));
                        tmp.addAll(busTimetables.get(bus.getReturn().getId()).getStops());
                    }
                }
            }

        } catch (Throwable e) {
            logger.error("", e);
        } finally {
            stops.clear();
            stops.addAll(tmp);
        }
        logger.info(System.currentTimeMillis() - begin + "ms");
    }

    static void scheduleUpdate() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                BusDataManagement::refreshBusTimeTable, 0, 1, TimeUnit.HOURS
        );
    }

    static Buses getBusRoutes() throws ExecutionException {
        return cacheBuses.get(MobilityDataServiceTrento.TRENTO);
    }

    static MapTimeTable getBusTimeTable(ComparableId comparableId) throws ExecutionException {
        return cacheBusTimetables.get(comparableId);
    }

    static Set<ComparableStop> getStops() {
        return stops;
    }

    static List<MapTimeTable> getBusTimeTables() {
        return new ArrayList<>(busTimetables.values());
    }
}