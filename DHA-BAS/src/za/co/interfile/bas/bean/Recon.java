package za.co.interfile.bas.bean;

import com.google.common.base.Objects;
import java.util.Set;

/**
 * @author Theuns Cloete
 */
public final class Recon {
    private final long reconID;
    private final String reconDate;
    private final Set<Long> deposits;
    private final Set<Long> dayEnds;
    private final long facilityID;

    public Recon(long reconID, String reconDate, Set<Long> deposits, Set<Long> dayEnds, long facilityID) {
        this.reconID = reconID;
        this.reconDate = reconDate;
        this.deposits = deposits;
        this.dayEnds = dayEnds;
        this.facilityID = facilityID;
    }

    public long getReconID() {
        return this.reconID;
    }

    public String getReconDate() {
        return this.reconDate;
    }

    public Set<Long> getDeposits() {
        return this.deposits;
    }

    public Set<Long> getDayEnds() {
        return this.dayEnds;
    }

    public long getFacilityID() {
        return this.facilityID;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(Recon.class)
            .add("reconID", this.reconID)
            .add("reconDate", this.reconDate)
            .add("deposits", this.deposits)
            .add("dayEnds", this.dayEnds)
            .add("facilityID", this.facilityID)
            .toString();
    }
}
