package photogift.server.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

@Entity
@Cache
public class TopGiver {

    public static String kind = "photogift#topgiver";

    public static Key<TopGiver> key(long id) {
        return Key.create(TopGiver.class, id);
    }

    /**
     * Copied from {@link User#id}.
     */
    @Id
    public Long ownerUserId;

    /**
     * The total count of all votes to the whole Gifts of this Giver.
     */
    @Index
    public int count;

    /**
     * Copied from {@link User#googleDisplayName}.
     */
    public String googleDisplayName;

    /**
     * Copied from {@link User#googlePhotoUrl}.
     */
    public String googlePhotoUrl;


}
