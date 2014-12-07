package photogift.server.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Represents a single vote by a single User on a single Gift.
 */
@Entity
public class TouchingGift {

    public static String kind = "photogift#touching_gift";

    public static Key<TouchingGift> key(long id) {
        return Key.create(TouchingGift.class, id);
    }

    @Id
    public Long id;

    /**
     * ID of Vote's Owner.
     */
    @Index
    public Long touchedUserId;

    /**
     * ID of the Gift to which this Vote was made.
     */
    @Index
    public Long giftId;
}
