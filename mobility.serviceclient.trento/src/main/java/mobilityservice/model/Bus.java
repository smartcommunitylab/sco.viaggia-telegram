package mobilityservice.model;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class Bus {

    private ComparableRoute busDirect;
    private ComparableRoute busReturn;

    private String routeShortName;

    public boolean addRoute(ComparableRoute route) {
        if (routeShortName == null)
            this.routeShortName = route.getRouteShortName();

        if (!route.getRouteShortName().equals(routeShortName))
            return false;

        String id = route.getId().getId();
        switch (id.charAt(id.length() - 1)) {
            case 'c':
            case 'C':
            case 'a':
            case 'A':
                setDirect(route);
                break;

            case 'r':
            case 'R':
                setReturn(route);
                break;

            default:
                throw new IllegalArgumentException(id);
        }

        return true;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public ComparableRoute getDirect() {
        return this.busDirect;
    }

    private void setDirect(ComparableRoute comparableRoute) {
        busDirect = comparableRoute;
    }

    public ComparableRoute getReturn() {
        return this.busReturn;
    }

    private void setReturn(ComparableRoute comparableRoute) {
        busReturn = comparableRoute;
    }

    ComparableRoute getWithId(ComparableId id) {
        if (busDirect.getId().equals(id))
            return busDirect;

        if (hasReturn() && busReturn.getId().equals(id))
            return busReturn;

        return null;
    }

    public boolean hasReturn() {
        return busReturn != null;
    }

    public boolean isDirect(ComparableId routeId) {
        return getDirect().getId().equals(routeId);
    }

    public boolean isReturn(ComparableId routeId) {
        return hasReturn() && getReturn().getId().equals(routeId);
    }
}
