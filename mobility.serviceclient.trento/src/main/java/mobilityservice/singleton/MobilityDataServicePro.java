package mobilityservice.singleton;

import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import mobilityservice.model.ComparableId;
import mobilityservice.model.ComparableStop;
import mobilityservice.model.MapTimeTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class MobilityDataServicePro {

    private static final MobilityDataService service = MobilityDataServiceSingleton.getInstance();

    MobilityDataServicePro() {
    }

    private static String correctsId(String routeId) {
        Map<String, String> routeCorrectId = new HashMap<>();

        routeCorrectId.put("CMA", "CMa");
        routeCorrectId.put("CMR", "CMr");
        routeCorrectId.put("%20GA", "GA");
        routeCorrectId.put("%20GR", "GR");
        routeCorrectId.put("%20AC", "_A");
        routeCorrectId.put("%20BC", "_B");
        routeCorrectId.put("NPC", "NPA");
        routeCorrectId.put("02C", "02");
        routeCorrectId.put("01A", "1A");
        routeCorrectId.put("01R", "1R");
        routeCorrectId.put("FunA", "FUTSA");
        routeCorrectId.put("FunR", "FUTSR");

        return routeCorrectId.get(routeId) != null ? routeCorrectId.get(routeId) : routeId;
    }

    public MapTimeTable getMapTimeTable(ComparableId routeId, Long when, String token) throws MobilityServiceException {
        List<ComparableStop> comparableStops = new ArrayList<>();
        try {
            service.getStops(routeId.getAgency(), correctsId(routeId.getId()), null).forEach(stop -> comparableStops.add(new ComparableStop(stop)));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            return new MapTimeTable(routeId, comparableStops, service.getTimeTable(routeId.getAgency(), routeId.getId(), when, token));
        } catch (Throwable e) {
            e.printStackTrace();
            return new MapTimeTable(routeId, new ArrayList<>(), service.getTimeTable(routeId.getAgency(), routeId.getId(), when, token));
        }
    }

    public MapTimeTable updateDelays(MapTimeTable mapTimeTable) throws MobilityServiceException {
        mapTimeTable.updateDelays(service.getDelays(mapTimeTable.getRouteId().getAgency(), mapTimeTable.getRouteId().getId(), null));
        return mapTimeTable;
    }
}
