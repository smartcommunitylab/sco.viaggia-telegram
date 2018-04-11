package mobilityservice.model;

import eu.trentorise.smartcampus.mobilityservice.model.Delay;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

/**
 * @author Luca Mosetti
 * @since 02/2018
 */
public class Trip {

    private final String tripId;
    private TemporalAmount delay;

    public Trip(String tripId, Delay delay) {
        this.tripId = tripId;
        this.setDelay(delay);
    }

    private long tryParse(String string) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getTripId() {
        return tripId;
    }

    public TemporalAmount getDelay() {
        return delay;
    }

    void setDelay(Delay delay) {
        String delayString;

        if (delay != null && delay.getValues() != null && (delayString = delay.getValues().get(CreatorType.SERVICE)) != null) {
            this.delay = Duration.ofMinutes(tryParse(delayString));
        } else {
            this.delay = Duration.ZERO;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass() && getTripId().equals(((Trip) obj).getTripId());
    }
}
