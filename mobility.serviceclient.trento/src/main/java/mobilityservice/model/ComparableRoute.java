package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class ComparableRoute {

    private final ComparableId id;
    private final String routeShortName;
    private final String routeLongName;

    public ComparableRoute(Route route) {
        this.id = new ComparableId(route.getId());
        this.routeLongName = route.getRouteLongName();
        this.routeShortName = route.getRouteShortName();
    }

    public String toString() {
        return !this.getRouteLongName().isEmpty() && !this.getRouteShortName().isEmpty() ? this.getRouteShortName() + " - " + this.getRouteLongName() : super.toString();
    }

    public ComparableId getId() {
        return this.id;
    }

    public String getRouteShortName() {
        return this.routeShortName;
    }

    public String getRouteLongName() {
        return this.routeLongName;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass() && this.getId().equals(((ComparableRoute) obj).getId());
    }
}
