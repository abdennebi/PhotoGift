package photogift.server.config;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppEngineServicesConfig {

    @Bean
    public BlobstoreService blobstoreService() {
        return BlobstoreServiceFactory.getBlobstoreService();
    }

    @Bean
    public DatastoreService datastoreService() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    @Bean
    public ImagesService imagesService() {
        return ImagesServiceFactory.getImagesService();
    }

    @Bean
    public MemcacheService MemcacheService() {
        return MemcacheServiceFactory.getMemcacheService();
    }

    @Bean
    public Index giftIndex() {
        return SearchServiceFactory.getSearchService().getIndex(IndexSpec.newBuilder().setName("gift_index"));
    }


}
