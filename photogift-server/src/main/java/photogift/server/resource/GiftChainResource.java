package photogift.server.resource;

import photogift.server.domain.Gift;
import photogift.server.domain.GiftChain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Resource representing the {@link GiftChain} class to be serialized in JSONand send to client.
 */
public interface GiftChainResource {

    Long getGiftChainId();

    Date getCreationDate();

    Date getUpdateDate();

    String getTitle();

    String getThumbnailUrl();

    public List<GiftResource> getGiftList();

    public List<Long> getGiftIds();


    public class Impl implements GiftChainResource {

        private final GiftChain giftChain;

        public Impl(GiftChain giftChain) {
            this.giftChain = giftChain;
        }

        @Override
        public Long getGiftChainId() {
            return giftChain.id;
        }

        @Override
        public Date getCreationDate() {
            return giftChain.creationDate;
        }

        @Override
        public Date getUpdateDate() {
            return giftChain.updateDate;
        }

        @Override
        public String getTitle() {
            return giftChain.title;
        }

        @Override
        public String getThumbnailUrl() {
            return giftChain.thumbnailUrl;
        }

        @Override
        public List<GiftResource> getGiftList() {
            List<GiftResource> giftResources = new ArrayList<GiftResource>();

            if (giftChain.giftList != null) {
                for (Gift gift : giftChain.giftList) {
                    giftResources.add(GiftResource.Assembler.toResource(gift));
                }
            }
            return giftResources;
        }

        @Override
        public List<Long> getGiftIds() {
            return giftChain.giftIds;
        }
    }

    public class Assembler {

        public static GiftChainResource toResource(GiftChain entity) {
            return new Impl(entity);
        }
    }
}
