package viaggia.utils;

/**
 * @author Luca Mosetti
 * @since 02/2018
 */
public class Distance<T> implements Comparable<Distance> {

    private final T value;
    private final Double distance;

    Distance(T value, double distance) {
        this.value = value;
        this.distance = distance;
    }

    public T getValue() {
        return value;
    }

    public Double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Distance o) {
        return distance.compareTo(o.getDistance());
    }
}
