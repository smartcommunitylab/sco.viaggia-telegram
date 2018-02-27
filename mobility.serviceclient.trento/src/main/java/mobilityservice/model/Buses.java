package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class Buses extends ArrayList<Bus> {

    public void putAll(List<Route> routes) {
        Bus bus;

        for (ComparableRoute route : routes.stream().map(ComparableRoute::new).collect(Collectors.toList())) {
            bus = this.getWithShortName(route.getRouteShortName());
            if (bus == null) this.add(bus = new Bus());
            bus.addRoute(route);
        }
    }

    public ComparableRoute getRouteWithId(ComparableId id) {
        return this.stream().map(bus -> bus.getWithId(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Bus getWithRouteId(ComparableId id) {
        return this.stream().filter(bus -> bus.getWithId(id) != null).findFirst().orElse(null);
    }

    public Bus getWithShortName(String shortName) {
        return this.stream().filter(bus -> bus.getRouteShortName().equals(shortName)).findFirst().orElse(null);
    }

    public List<String> getShortNames() {
        return this.stream().map(Bus::getRouteShortName).collect(Collectors.toList());
    }

    public Buses subBuses(String filter) {
        return this.stream().filter(bus -> bus.getDirect().getRouteShortName().toLowerCase().contains(filter.toLowerCase())).collect(Collectors.toCollection(Buses::new));
    }

    public ComparableRoutes getDirects() {
        return this.stream().map(Bus::getDirect).collect(Collectors.toCollection(ComparableRoutes::new));
    }
}

