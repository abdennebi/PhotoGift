package photogift.server.resource;

import photogift.server.domain.TopGiver;

public interface TopGiverResource {

    Long getUserId();

    String getDisplayName();

    String getPhotoUrl();

    int getCount();

    public class Impl implements TopGiverResource {

        private final TopGiver topGiver;

        public Impl(TopGiver topGiver) {
            this.topGiver = topGiver;
        }

        @Override
        public Long getUserId() {
            return topGiver.ownerUserId;
        }

        @Override
        public String getDisplayName() {
            return topGiver.googleDisplayName;
        }

        @Override
        public String getPhotoUrl() {
            return topGiver.googlePhotoUrl;
        }

        @Override
        public int getCount() {
            return topGiver.count;
        }
    }

    public class Assembler {

        public static TopGiverResource toResource(TopGiver topGiver) {
            return new Impl(topGiver);
        }
    }
}
