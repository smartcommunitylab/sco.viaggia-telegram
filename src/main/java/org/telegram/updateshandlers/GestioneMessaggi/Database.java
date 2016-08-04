package org.telegram.updateshandlers.GestioneMessaggi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import eu.trentorise.smartcampus.mobilityservice.model.TaxiContact;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import org.telegram.telegrambots.api.objects.Location;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by gekoramy
 */
public class Database {

    // TODO List of errors in Autobus List:
    // TODO - A, B, C   : always return a empty TimeTable
    // TODO - 1, 14R    : List<Stops>.size() != List<Times>.size()
    // TODO - FuR       : doesn't appear in List<Route>

    // region final

    private static final String AGENCY_AUTOBUS = "12";
    private static final String AGENCY_TRAINS_BV = "6";
    private static final String AGENCY_TRAINS_TB = "5";
    private static final String AGENCY_TRAINS_TM = "10";
    private static final String AGENCY_PARKINGS = "COMUNE_DI_TRENTO";
    private static final String AGENCY_BIKESHARINGS = "BIKE_SHARING_TOBIKE_TRENTO";
    private static final String SERVER_URL = "https://tn.smartcommunitylab.it/core.mobility";

    // endregion final

    private static MobilityDataService dataService = new MobilityDataService(SERVER_URL);

    private static LoadingCache<String, List<TaxiContact>> cacheTaxiContacts;
    private static LoadingCache<String, List<Parking>> cacheParkings;
    private static LoadingCache<String, List<Parking>> cacheBikeSharings;
    private static LoadingCache<String, List<Route>> cacheAutobusRoutes;
    private static LoadingCache<String, List<Route>> cacheTrainsRoutes;
    private static LoadingCache<String, TimeTable> cacheAutobusTimetables;
    private static LoadingCache<String, TimeTable> cacheTrainTimetables;

    static {
        cacheTaxiContacts = CacheBuilder.newBuilder().expireAfterWrite(12, TimeUnit.HOURS).build(new CacheLoader<String, List<TaxiContact>>() {
            @Override
            public List<TaxiContact> load(String s) throws Exception {
                return downloadTaxiContacts();
            }
        });

        cacheParkings = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, List<Parking>>() {
            @Override
            public List<Parking> load(String agencyId) throws Exception {
                return downloadParkings(agencyId);
            }
        });

        cacheBikeSharings = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, List<Parking>>() {
            @Override
            public List<Parking> load(String agencyId) throws Exception {
                return downloadBikeSharing(agencyId);
            }
        });

        cacheAutobusRoutes = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, List<Route>>() {
            @Override
            public List<Route> load(String agencyId) throws Exception {
                return downloadAutbusRoute(agencyId);
            }
        });

        cacheTrainsRoutes = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, List<Route>>() {
            @Override
            public List<Route> load(String agencyId) throws Exception {
                return downloadTrainsRoute(agencyId);
            }
        });


        cacheAutobusTimetables = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<String, TimeTable>() {
            @Override
            public TimeTable load(String routeId) throws Exception {
                return downloadAutobusTimetable(routeId);
            }
        });

        cacheTrainTimetables = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<String, TimeTable>() {
            @Override
            public TimeTable load(String routeId) throws Exception {
                return downloadTrainTimetable(routeId);
            }
        });
    }

    // region utilities

    private static List<Route> allTrains() throws ExecutionException {
        List<String> agencyIds = Arrays.asList(AGENCY_TRAINS_BV, AGENCY_TRAINS_TB, AGENCY_TRAINS_TM);
        List<Route> trains = new ArrayList<>();

        for (String agency : agencyIds)
            trains.addAll(cacheTrainsRoutes.get(agency));

        return trains;
    }

    private static String capitalize(String s) {
        String text = "";
        String[] words = s.split("\\s");
        for (String string : words)
            text += (string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase() + " ");
        return text.substring(0, text.length() - 1);
    }

    private static String addSpace(String s) {
        return s.replace("/", " / ").trim();
    }

    private static String getTrainAgencyId(String trainId) throws ExecutionException {
        List<String> agencyIds = Arrays.asList(AGENCY_TRAINS_BV, AGENCY_TRAINS_TB, AGENCY_TRAINS_TM);

        for (String ageny : agencyIds)
            for (Route r : cacheTrainsRoutes.get(ageny))
                if (r.getId().getId().equals(trainId))
                    return ageny;

        return null;
    }

    private static String getAutobusRouteId(String routeId, Boolean isAndata) throws ExecutionException {
        for (Route route : cacheAutobusRoutes.get(AGENCY_AUTOBUS)) {
            if (route.getRouteShortName().equals(routeId))
                if (route.getId().getId().endsWith("C"))
                    return route.getId().getId();
                else if (isAndata && route.getId().getId().endsWith("A"))
                    return route.getId().getId();
                else if (!isAndata && route.getId().getId().endsWith("R"))
                    return route.getId().getId();
        }
        return null;
    }

    // endregion utilities

    // region download

    private static List<TaxiContact> downloadTaxiContacts() throws SecurityException, MobilityServiceException {
        return dataService.getTaxiAgencyContacts(null);
    }

    private static List<Parking> downloadParkings(String agencyId) throws SecurityException, MobilityServiceException {
        return dataService.getParkings(agencyId, null);
    }

    private static List<Parking> downloadBikeSharing(String agencyId) throws SecurityException, MobilityServiceException {
        return dataService.getBikeSharings(agencyId, null);
    }

    private static List<Route> downloadAutbusRoute(String agencyId) throws SecurityException, MobilityServiceException {

        List<Route> autobus = dataService.getRoutes(agencyId, null);

        Map<String, String> routeSymId;
        {
            routeSymId = new HashMap<>();
            routeSymId.put("_A", "%20Ac");  // ERROR, tried : '%20', '%2520', '% ', '_' + 'AC', 'Ac'
            routeSymId.put("_B", "%20Bc");  // ERROR, tried : '%20', '%2520', '% ', '_' + 'AC', 'Ac'
            routeSymId.put("NPA", "NPC");
            routeSymId.put("02", "02C");
            routeSymId.put("1A", "01A");
            routeSymId.put("1R", "01R");
            routeSymId.put("FUTSA", "FunA");
            routeSymId.put("FUTSR", "FunR");
        }

        for (Route route : autobus)
            if (routeSymId.containsKey(route.getId().getId()))
                route.getId().setId(routeSymId.get(route.getId().getId()));

        return autobus;
    }

    private static List<Route> downloadTrainsRoute(String agencyId) throws SecurityException, MobilityServiceException {
        List<Route> list = new ArrayList<>();

        list.addAll(dataService.getRoutes(agencyId, null));

        for (Route r : list) {
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(capitalize(r.getRouteLongName()));
        }

        return list;
    }

    private static TimeTable downloadAutobusTimetable(String routeId) throws MobilityServiceException {
        return dataService.getTimeTable(AGENCY_AUTOBUS, routeId, System.currentTimeMillis(), null);
    }

    private static TimeTable downloadTrainTimetable(String routeId) throws MobilityServiceException, ExecutionException {
        return dataService.getTimeTable(getTrainAgencyId(routeId), routeId, System.currentTimeMillis(), null);
    }

    // endregion download

    // region gets

    public static List<TaxiContact> getTaxiContacts() throws ExecutionException {
        return cacheTaxiContacts.get("0");
    }

    public static List<Route> getAutobusRoutes() throws ExecutionException {
        return cacheAutobusRoutes.get(AGENCY_AUTOBUS);
    }

    public static List<Route> getTrainsRoutes() throws ExecutionException {
        return allTrains();
    }

    public static List<Parking> getParkings() throws ExecutionException {
        return cacheParkings.get(AGENCY_PARKINGS);
    }

    public static List<Parking> getBikeSharings() throws ExecutionException {
        return cacheBikeSharings.get(AGENCY_BIKESHARINGS);
    }

    public static TimeTable getAutobusTimetable(String routeId) throws ExecutionException {
        return cacheAutobusTimetables.get(routeId);
    }

    public static TimeTable getTrainTimetable(String routeId) throws ExecutionException {
        return cacheTrainTimetables.get(routeId);
    }

    // endregion gets

    // region finds

    public static List<Parking> findNear(List<Parking> zone, Location loc) {
        List<Parking> near = new ArrayList<>();

        for (Parking el : zone)
            if (DistanceCalculator.distance(loc.getLatitude(), loc.getLongitude(), el.getPosition()[0], el.getPosition()[1], "K") <= 1.5)
                near.add(el);
        return near;
    }

    public static String findAutobusAndataRouteId(String routeId) throws ExecutionException {
        return getAutobusRouteId(routeId, true);
    }

    public static String findAutobusRitornoRouteId(String routeId) throws ExecutionException {
        return getAutobusRouteId(routeId, false);
    }

    public static String findTrainRouteId(String routeId) throws ExecutionException {
        for (Route route : allTrains())
            if (route.getRouteLongName().equals(routeId))
                return route.getId().getId();
        return null;
    }

    public static int findCurrentIndex(TimeTable timeTable) {
        int hours, minutes;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        String string = "";
        for (List<String> time : timeTable.getTimes()) {
            for (int i = 0; string.isEmpty(); i++)
                string = time.get(i);

            hours = Integer.parseInt(string.substring(0, string.indexOf(':')));
            minutes = Integer.parseInt(string.substring(string.indexOf(':') + 1));

            if (hours >= cal.get(Calendar.HOUR_OF_DAY) || hours == cal.get(Calendar.HOUR_OF_DAY) && minutes >= cal.get(Calendar.MINUTE))
                return timeTable.getTimes().indexOf(time);
            string = "";
        }

        return 0;
    }

    // endregion finds

}
