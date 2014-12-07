package photogift.server.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Represents a single vote by a single User on a single Gift.
 */
@Entity
public class InappropriateGift {

    public static String kind = "photogift#inappropriate_gift";

    public static Key<InappropriateGift> key(long id) {
        return Key.create(InappropriateGift.class, id);
    }

    @Id
    public Long id;

    /**
     * ID of Vote's Owner.
     */
    @Index
    public Long flaggedByUserId;

    /**
     * ID of the Gift to which this Vote was made.
     */
    @Index
    public Long giftId;
}
