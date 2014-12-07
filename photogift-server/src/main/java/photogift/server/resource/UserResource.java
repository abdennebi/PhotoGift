package photogift.server.resource;

import photogift.server.domain.User;

import java.util.Date;
import java.util.List;

public interface UserResource {

    Long getUserId();

    String getGoogleUserId();

    String getGoogleDisplayName();

    String getGooglePhotoUrl();

    String getGoogleProfileUrl();

    Date getLastUpdated();

    List<Long> getTouchingGifts();

    public class Impl implements UserResource {

        private final User user;

        public Impl(User user) {
            this.user = user;
        }

        @Override
        public Long getUserId() {
            return user.id;
        }

        @Override
        public String getGoogleUserId() {
            return user.googleUserId;
        }

        @Override
        public String getGoogleDisplayName() {
            return user.googleDisplayName;
        }

        @Override
        public String getGooglePhotoUrl() {
            return user.googlePhotoUrl;
        }

        @Override
        public String getGoogleProfileUrl() {
            return user.googleProfileUrl;
        }

        @Override
        public Date getLastUpdated() {
            return user.lastUpdated;
        }

        @Override
        public List<Long> getTouchingGifts() {
            return user.touchingGifts;
        }
    }

    public class Assembler {

        public static UserResource toResource(User user) {
            return new Impl(user);
        }

    }
}
