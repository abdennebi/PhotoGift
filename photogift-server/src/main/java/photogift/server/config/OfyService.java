package photogift.server.config;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import photogift.server.domain.*;

/**
 * Service wrapper for Objectify, allowing us to register our models.
 *
 * @author vicfryzel@google.com (Vic Fryzel)
 */
public class OfyService {
    // Register our models with Objectify.  If you add a new model, make sure to
    // register it here as well.
    static {
        factory().register(Gift.class);
        factory().register(User.class);
        factory().register(TouchingGift.class);
        factory().register(GiftChain.class);
        factory().register(TopGiver.class);
        factory().register(InappropriateGift.class);
    }

    /**
     * @return Objectify instance to use for datastore interaction.
     */
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    /**
     * @return Factory for Objectify instances.
     */
    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
