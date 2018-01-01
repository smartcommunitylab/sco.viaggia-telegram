package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Luca Mosetti in 2017
 */
public class ComparableRoutes extends ArrayList<ComparableRoute> {

    public ComparableRoutes() {
    }

    public ComparableRoutes(List<ComparableRoute> list) {
        for (ComparableRoute r : list) {
            this.put(r);
        }
    }

    private void put(ComparableRoute r) {
        if (this.contains(r))
            this.set(this.indexOf(r), r);
        else
            this.add(r);
    }

    public void putAll(List<Route> routes) {
        for (Route r : routes) {
            ComparableRoute cr = new ComparableRoute(r);
            put(cr);
        }
    }

    public List<String> getLongNames() {
        return this.stream().map(ComparableRoute::getRouteLongName).collect(Collectors.toList());
    }

    public List<String> getShortNames() {
        return this.stream().map(ComparableRoute::getRouteShortName).collect(Collectors.toList());
    }

    public ComparableRoute getWithLongName(String longName) {
        for (ComparableRoute route : this) {
            if (route.getRouteLongName().equals(longName))
                return route;
        }

        return null;
    }

    public ComparableRoute getWithId(ComparableId comparableId) {
        for (ComparableRoute route : this) {
            if (route.getId().equals(comparableId))
                return route;
        }

        return null;
    }

    public ComparableRoutes subRoutesLongName(String filter) {
        return this.stream().filter(route -> route.getRouteLongName().toLowerCase().contains(filter.toLowerCase())).collect(Collectors.toCollection(ComparableRoutes::new));
    }

    public ComparableRoutes subRoutesShortName(String filter) {
        return this.stream().filter(route -> route.getRouteShortName().toLowerCase().contains(filter.toLowerCase())).collect(Collectors.toCollection(ComparableRoutes::new));
    }
}
