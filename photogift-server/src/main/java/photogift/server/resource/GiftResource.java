package photogift.server.resource;


import photogift.server.domain.Gift;

import java.util.Date;

public interface GiftResource {

    Long getGiftId();

    Long getGiftChainId();

    String getTitle();

    String getText();

    String getImageUrl();

    String getThumbnailUrl();

    String getOwnerDisplatName();

    String getOwnerProfileUrl();

    String getOwnerProfilePhoto();

    Date created();

    int getTouchedCount();

    boolean isInappropriate();

    Date getCreationDate();

//    boolean userAlreadyTouched();

    public class Impl implements GiftResource {

        private final Gift gift;

        public Impl(Gift gift) {
            this.gift = gift;
        }

        @Override
        public String getText() {
            return gift.text;
        }

        @Override
        public Long getGiftId() {
            return gift.id;
        }

        @Override
        public Long getGiftChainId() {
            return gift.giftChainId;
        }

        @Override
        public String getTitle() {
            return gift.title;
        }

        @Override
        public String getImageUrl() {
            return gift.fullsizeUrl;
        }

        @Override
        public String getThumbnailUrl() {
            return gift.thumbnailUrl;
        }

        @Override
        public String getOwnerDisplatName() {
            return gift.ownerDisplayName;
        }

        @Override
        public String getOwnerProfileUrl() {
            return gift.ownerProfileUrl;
        }

        @Override
        public String getOwnerProfilePhoto() {
            return gift.ownerProfilePhoto;
        }

        @Override
        public Date created() {
            return gift.created;
        }

        @Override
        public int getTouchedCount() {
            return gift.touchedCount;
        }

        @Override
        public boolean isInappropriate() {
            return false;
        }

        @Override
        public Date getCreationDate() {
            return gift.created;
        }

    }

    public class Assembler {

        public static GiftResource toResource(Gift gift) {
            return new Impl(gift);

        }
    }
}
