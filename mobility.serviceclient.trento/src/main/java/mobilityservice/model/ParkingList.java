package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luca Mosetti
 * @since 2017
 */
public class ParkingList extends ArrayList<Parking> {

    public Parking getSimilar(String name) {
        for (Parking p : this) {
            if (p.getName().startsWith(name))
                return p;
        }

        return null;
    }

    public Parking get(String name) {
        for (Parking p : this) {
            if (p.getName().equals(name))
                return p;
        }

        return null;
    }

    public void putAll(Collection<Parking> parkingCollection) {
        for (Parking p : parkingCollection) {
            if (this.contains(p))
                this.set(this.indexOf(p), p);
            else
                this.add(p);
        }
    }

    public List<String> getNames() {
        return this.stream().map(Parking::getName).collect(Collectors.toList());
    }

    public List<Parking> parkingSubList(String filter) {
        return this.stream().filter(p -> p.getName().toLowerCase().contains(filter)).collect(Collectors.toList());
    }
}
