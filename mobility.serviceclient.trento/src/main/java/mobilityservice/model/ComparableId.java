package mobilityservice.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;

/**
 * Created by Luca Mosetti on 2017
 */
public class ComparableId extends Id {

    public ComparableId(Id id) {
        super.setAgency(id.getAgency());
        super.setId(id.getId());
    }

    public ComparableId(String id, String agency) {
        super.setId(id);
        super.setAgency(agency);
    }

    @Override
    public int hashCode() {
        return super.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass() && this.getId().equals(((ComparableId) obj).getId());
    }
}