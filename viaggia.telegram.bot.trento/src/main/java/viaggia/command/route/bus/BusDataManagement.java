package viaggia.command.route.bus;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.Bus;
import mobilityservice.model.Buses;
import mobilityservice.model.ComparableId;
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
 * Created by Luca Mosetti in 2017
 */
class BusDataManagement {

    private static final Logger logger = LoggerFactory.getLogger(BusDataManagement.class);
    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final MobilityDataServicePro service = MobilityDataServiceProSingleton.getInstance();
    private static final LoadingCache<String, Buses> cacheBuses;
    private static final LoadingCache<ComparableId, MapTimeTable> cacheTrentoBusTimetables;

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

        cacheTrentoBusTimetables = CacheBuilder.newBuilder()
                .build(new CacheLoader<ComparableId, MapTimeTable>() {
                    @Override
                    public MapTimeTable load(ComparableId comparableId) throws Exception {
                        return service.getMapTimeTable(comparableId.getAgency(), comparableId.getId(), System.currentTimeMillis(), null);
                    }
                });
    }

    private static void refreshBusTimeTable() {
        try {
            for (Bus bus : getBusRoutes()) {
                cacheTrentoBusTimetables.refresh(bus.getDirect().getId());

                if (bus.hasReturn()) {
                    cacheTrentoBusTimetables.refresh(bus.getReturn().getId());
                }
            }
        } catch (ExecutionException e) {
            logger.error(e.getMessage());
        }
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
        return cacheTrentoBusTimetables.get(comparableId);
    }
}