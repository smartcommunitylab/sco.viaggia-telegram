package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luca Mosetti
 * @since 2017
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
        routes.stream().map(ComparableRoute::new).forEach(this::put);
    }

    public List<String> getLongNames() {
        return this.stream().map(ComparableRoute::getRouteLongName).collect(Collectors.toList());
    }

    public List<String> getShortNames() {
        return this.stream().map(ComparableRoute::getRouteShortName).collect(Collectors.toList());
    }

    public ComparableRoute getWithLongName(String longName) {
        return this.stream().filter(route -> route.getRouteLongName().equals(longName)).findFirst().orElse(null);

    }

    public ComparableRoute getWithId(ComparableId comparableId) {
        return this.stream().filter(route -> route.getId().equals(comparableId)).findFirst().orElse(null);

    }

    public ComparableRoutes subRoutesLongName(String filter) {
        return this.stream().filter(route -> route.getRouteLongName().toLowerCase().contains(filter.toLowerCase())).collect(Collectors.toCollection(ComparableRoutes::new));
    }

    public ComparableRoutes subRoutesShortName(String filter) {
        return this.stream().filter(route -> route.getRouteShortName().toLowerCase().contains(filter.toLowerCase())).collect(Collectors.toCollection(ComparableRoutes::new));
    }
}
