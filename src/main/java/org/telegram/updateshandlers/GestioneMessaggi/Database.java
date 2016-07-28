package org.telegram.updateshandlers.GestioneMessaggi;

import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import eu.trentorise.smartcampus.mobilityservice.model.TaxiContact;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.*;
import org.telegram.telegrambots.api.objects.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private static final String AUTOBUS_ID = "12";
    private static final String TRAINS_ID_BV = "6";
    private static final String TRAINS_ID_TB = "5";
    private static final String TRAINS_ID_TM = "10";
    private static final String SERVER_URL = "https://tn.smartcommunitylab.it/core.mobility";
    private static List<Parking> parkings = new ArrayList<>();
    private static List<Parking> bikeSharings = new ArrayList<>();
    private static List<TaxiContact> taxi = new ArrayList<>();
    private static List<Route> autobus = new ArrayList<>();
    private static List<Route> trains_BV = new ArrayList<>();
    private static List<Route> trains_TM = new ArrayList<>();
    private static List<Route> trains_TB = new ArrayList<>();
    private static List<Route> trains = new ArrayList<>();
    private static MobilityDataService dataService = new MobilityDataService(SERVER_URL);

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

    public static List<TaxiContact> getTaxiInfo() throws SecurityException, MobilityServiceException {
        return taxi = dataService.getTaxiAgencyContacts(null);
    }

    public static List<Parking> getParkings() throws SecurityException, MobilityServiceException {
        return parkings = dataService.getParkings("COMUNE_DI_TRENTO", null);
    }

    public static List<Parking> getBikeSharing() throws SecurityException, MobilityServiceException {
        return bikeSharings = dataService.getBikeSharings("BIKE_SHARING_TOBIKE_TRENTO", null);
    }

    public static List<Parking> getNear(List<Parking> zone, Location loc) {
        List<Parking> near = new ArrayList<>();

        for (Parking el : zone)
            if (DistanceCalculator.distance(loc.getLatitude(), loc.getLongitude(), el.getPosition()[0], el.getPosition()[1], "K") <= 1.5)
                near.add(el);
        return near;
    }

    // region TODO

    public static List<Route> getAutbusRoute() throws SecurityException, MobilityServiceException {

        autobus = dataService.getRoutes(AUTOBUS_ID, null);

        Map<String, String> routeSymId;
        {
            routeSymId = new HashMap<String, String>();
            routeSymId.put("_A", "%20AC");  // TODO ERROR, tried '%20', '%2520', '% ', '_'
            routeSymId.put("_B", "%20BC");  // TODO ERROR, tried '%20', '%2520', '% ', '_'
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

    public static List<Route> getTrainsRoute() throws SecurityException, MobilityServiceException {
        trains.clear();
        trains_BV.clear();
        trains_TB.clear();
        trains_TM.clear();


        trains_BV.addAll(dataService.getRoutes(TRAINS_ID_BV, null));
        trains_TB.addAll(dataService.getRoutes(TRAINS_ID_TB, null));
        trains_TM.addAll(dataService.getRoutes(TRAINS_ID_TM, null));

        for (Route r : trains_BV) {
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(capitalize(r.getRouteLongName()));
        }


        for (Route r : trains_TB) {
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(capitalize(r.getRouteLongName()));
        }

        for (Route r : trains_TM) {
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(capitalize(r.getRouteLongName()));
        }


        trains.addAll(trains_BV);
        trains.addAll(trains_TB);
        trains.addAll(trains_TM);

        return trains;
    }

    public static List<Stop> getStopAutobus(String autobusID) throws SecurityException, MobilityServiceException {
        Id id = stopAutobusId(autobusID);
        if (id == null) System.err.println("NULL, mi spiace... Ritenta, sarai piu fortunato");
        return id == null ? null : dataService.getStops(AUTOBUS_ID, id.getId(), null);
    }

    public static List<Stop> getStopTrain(String trainID) throws SecurityException, MobilityServiceException {
        Id id = stopTrainID(trainID);
        String agencyID = trainAgencyId(trainID);

        return id == null || agencyID == null ? null : dataService.getStops(agencyID, id.getId(), null);

    }

    public static List<StopTime> getNextTrips(String agencyId, String routeId, String stopId) throws MobilityServiceException {
        return dataService.getStopTimes(agencyId, routeId, stopId, null);
    }

    private static Id stopAutobusId(String autobusID) {
        for (Route r : autobus)
            if (r.getRouteShortName().equals(autobusID))
                return r.getId();

        return null;
    }

    private static Id stopTrainID(String trainID) {
        for (Route r : trains)
            if (r.getRouteLongName().equals(trainID))
                return r.getId();

        return null;
    }

    private static String trainAgencyId(String autobusID) {
        for (Route r : trains_BV)
            if (r.getRouteLongName().equals(autobusID))
                return TRAINS_ID_BV;

        for (Route r : trains_TB)
            if (r.getRouteLongName().equals(autobusID))
                return TRAINS_ID_TB;

        for (Route r : trains_TM)
            if (r.getRouteLongName().equals(autobusID))
                return TRAINS_ID_TM;

        return null;
    }

    public static TimeTable getAutobusTimetable(String routeId, Boolean isAndata) throws MobilityServiceException {
        routeId = getRouteId(routeId, isAndata);

        if (routeId == null)
            return null;
        else {
            System.out.println(routeId);
            return dataService.getTimeTable(AUTOBUS_ID, routeId, System.currentTimeMillis(), null);
        }
    }

    private static String getRouteId(String routeId, Boolean isAndata) {
        for (Route route : autobus) {
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

    // endregion TODO

}
