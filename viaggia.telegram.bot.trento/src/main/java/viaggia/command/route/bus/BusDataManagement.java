package viaggia.command.route.bus;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
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
 * Created by Luca Mosetti on 2017
 */
/*package*/ class BusDataManagement {

    private static final Logger logger = LoggerFactory.getLogger(BusDataManagement.class);
    private static final MobilityDataServiceTrento trento = MobilityDataServiceTrentoSingleton.getInstance();
    private static final MobilityDataServicePro service = MobilityDataServiceProSingleton.getInstance();
    private static final Supplier<Buses> supplierBuses;
    private static final LoadingCache<ComparableId, MapTimeTable> cacheTrentoBusTimetables;

    static {
        supplierBuses = Suppliers.memoizeWithExpiration(() -> {
            try {
                return trento.getBuses();
            } catch (MobilityServiceException e) {
                e.printStackTrace();
            }

            return null;
        }, 1, TimeUnit.DAYS);

        cacheTrentoBusTimetables = CacheBuilder.newBuilder()
                .build(new CacheLoader<ComparableId, MapTimeTable>() {
                    @Override
                    public MapTimeTable load(ComparableId comparableId) throws Exception {
                        return service.getMapTimeTable(comparableId.getAgency(), comparableId.getId(), System.currentTimeMillis(), null);
                    }
                });
    }

    private static void refreshBusTimeTable(ComparableId comparableId) throws ExecutionException {
        cacheTrentoBusTimetables.refresh(comparableId);
    }

    /*package*/
    static void scheduleUpdate() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    try {
                        for (Bus bus : getBusRoutes()) {
                            refreshBusTimeTable(bus.getDirect().getId());
                            if (bus.hasReturn())
                                refreshBusTimeTable(bus.getReturn().getId());
                        }
                    } catch (ExecutionException e) {
                        logger.error(e.getMessage());
                    }
                }, 0, 1, TimeUnit.HOURS
        );
    }

    /*package*/
    static Buses getBusRoutes() throws ExecutionException {
        return supplierBuses.get();
    }

    /*package*/
    static MapTimeTable getBusTimeTable(ComparableId comparableId) throws ExecutionException {
        return cacheTrentoBusTimetables.get(comparableId);
    }
}