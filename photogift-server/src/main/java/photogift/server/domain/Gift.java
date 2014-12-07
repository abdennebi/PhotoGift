package photogift.server.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import java.util.Date;

@Entity
@Cache
public class Gift {

    public static String kind = "photogift#gift";

    @Id
    public Long id;

    public static Key<Gift> key(long id) {
        return Key.create(Gift.class, id);
    }

    public String title;

    public String text;

    public long giftChainId;

    @Index
    public Long ownerUserId;

    /**
     * Copied from {@link User#googleDisplayName}
     */
    public String ownerDisplayName;

    /**
     * Copied from {@link User#googleProfileUrl}
     */
    public String ownerProfileUrl;

    /**
     * Copied from {@link User#googlePhotoUrl}
     */
    public String ownerProfilePhoto;

    public String imageBlobKey;

    /**
     * Creation Date.
     */
    public Date created;

    public String fullsizeUrl;

    public String thumbnailUrl;

    public boolean inappropriate;


    /**
     * URL for interactive posts and deep linking to the photo of this Gift.
     */
    @Ignore
    public String photoContentUrl;

    /**
     * Default size of thumbnails.
     */
    @Ignore
    public static final int DEFAULT_THUMBNAIL_SIZE = 400;

    @Ignore
    public int touchedCount;


}
