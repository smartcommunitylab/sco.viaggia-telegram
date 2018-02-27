package mobilityservice.singleton;

import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import mobilityservice.model.*;
import mobilityservice.utils.AlphanumComparator;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class MobilityDataServiceTrento {

    private static final MobilityDataService service = MobilityDataServiceSingleton.getInstance();

    private static final String AGENCY_BUSES = "12";
    private static final String AGENCY_TRAINS_BV = "6";
    private static final String AGENCY_TRAINS_TB = "5";
    private static final String AGENCY_TRAINS_TM = "10";
    private static final String AGENCY_PARKING = "COMUNE_DI_TRENTO";
    private static final String AGENCY_BIKE = "BIKE_SHARING_TOBIKE_TRENTO";

    public static final String TRENTO = "TN";

    // region comparators

    private static final AlphanumComparator<Bus> BUS_COMPARATOR = new AlphanumComparator<Bus>() {
        @Override
        public int compare(Bus o1, Bus o2) {
            return compareString(o1.getRouteShortName(), o2.getRouteShortName());
        }
    };

    private static final AlphanumComparator<ComparableRoute> ROUTE_COMPARATOR = new AlphanumComparator<ComparableRoute>() {
        @Override
        public int compare(ComparableRoute o1, ComparableRoute o2) {
            return compareString(o1.getRouteShortName(), o2.getRouteShortName());
        }
    };

    private static final AlphanumComparator<it.sayservice.platform.smartplanner.data.message.otpbeans.Parking> PARKING_COMPARATOR = new AlphanumComparator<it.sayservice.platform.smartplanner.data.message.otpbeans.Parking>() {
        @Override
        public int compare(it.sayservice.platform.smartplanner.data.message.otpbeans.Parking o1, it.sayservice.platform.smartplanner.data.message.otpbeans.Parking o2) {
            return compareString(o1.getName(), o2.getName());
        }
    };

    // endregion comparators

    MobilityDataServiceTrento() {
    }

    private static List<Route> correctsIDs(List<Route> routes) {
        Map<String, String> routeCorrectId = new HashMap<>();

        routeCorrectId.put("CMa", "CMA");
        routeCorrectId.put("CMr", "CMR");
        routeCorrectId.put("GA", "%20GA");
        routeCorrectId.put("GR", "%20GR");
        routeCorrectId.put("_A", "%20AC");
        routeCorrectId.put("_B", "%20BC");
        routeCorrectId.put("NPA", "NPC");
        routeCorrectId.put("02", "02C");
        routeCorrectId.put("1A", "01A");
        routeCorrectId.put("1R", "01R");
        routeCorrectId.put("FUTSA", "FunA");
        routeCorrectId.put("FUTSR", "FunR");

        for (Route route : routes)
            if (routeCorrectId.containsKey(route.getId().getId()))
                route.getId().setId(routeCorrectId.get(route.getId().getId()));

        return routes;
    }

    private static List<Route> correctsTrainNames(List<Route> ComparableRoutes) {
        for (Route r : ComparableRoutes) {
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(WordUtils.capitalizeFully(r.getRouteLongName()));
        }

        return ComparableRoutes;
    }

    private static void correctsBikeNames(Parkings parkings) {
        for (Parking p : parkings) {
            p.setName(p.getName().replace(" - Trento", ""));
        }
    }

    private static String addSpace(String s) {
        return s.replace("/", " / ").trim();
    }

    public Buses getBuses() throws MobilityServiceException {
        Buses buses = new Buses();
        buses.putAll(correctsIDs(service.getRoutes(AGENCY_BUSES, null)));

        buses.sort(BUS_COMPARATOR);

        return buses;
    }

    public ComparableRoutes getTrains() throws MobilityServiceException {
        ComparableRoutes comparableRoutes = new ComparableRoutes();

        for (String agency : Arrays.asList(AGENCY_TRAINS_BV, AGENCY_TRAINS_TB, AGENCY_TRAINS_TM))
            comparableRoutes.putAll(correctsTrainNames(service.getRoutes(agency, null)));

        comparableRoutes.sort(ROUTE_COMPARATOR);

        return comparableRoutes;
    }

    public Parkings getParkings() throws MobilityServiceException {
        Parkings parkings = new Parkings();
        parkings.putAll(service.getParkings(AGENCY_PARKING, null));

        parkings.sort(PARKING_COMPARATOR);

        return parkings;
    }

    public Parkings getBikes() throws MobilityServiceException {
        Parkings bikes = new Parkings();
        bikes.putAll(service.getBikeSharings(AGENCY_BIKE, null));
        correctsBikeNames(bikes);

        bikes.sort(PARKING_COMPARATOR);

        return bikes;
    }
}
