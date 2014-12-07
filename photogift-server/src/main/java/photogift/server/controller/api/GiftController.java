package photogift.server.controller.api;


import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.DatastoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import photogift.server.domain.Gift;
import photogift.server.domain.TopGiver;
import photogift.server.domain.User;
import photogift.server.exception.BadStateException;
import photogift.server.filter.Authenticate;
import photogift.server.resource.GiftResource;
import photogift.server.resource.TopGiverResource;
import photogift.server.resource.TouchedCountResource;
import photogift.server.resource.UploadUrlResource;
import photogift.server.service.GiftService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static photogift.server.filter.Authenticate.getUserFromSession;

@Controller
@RequestMapping("/gifts")
public class GiftController {

    @Autowired
    private BlobstoreService blobstoreService;

    @Autowired
    private DatastoreService datastoreService;

    @Autowired
    private GiftService giftService;

    /**
     * Returns a photo representation.
     */
    @RequestMapping(value = "/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GiftResource> getGift(@PathVariable Long id) {

        Gift gift = giftService.read(id);

        return new ResponseEntity<GiftResource>(GiftResource.Assembler.toResource(gift), OK);
    }

    @RequestMapping(value = "/{id}/touchedcount", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TouchedCountResource> getTouchedCount(@PathVariable Long id) {

        int count = giftService.getTouchedCount(id);

        return new ResponseEntity<TouchedCountResource>(TouchedCountResource.Assembler.toResource(id, count), OK);
    }

    @RequestMapping(value = "/{id}/touched/{touched}", method = POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GiftResource> setTouched(@PathVariable Long id, @PathVariable boolean touched, HttpServletRequest request) {

        User user = getUserFromSession(request);

        if (touched) giftService.setTouched(id, user);

        Gift gift = giftService.read(id);

        return new ResponseEntity<GiftResource>(GiftResource.Assembler.toResource(gift), OK);
    }

    @RequestMapping(value = "/{id}/inappropriate/{inappropriate}", method = POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GiftResource> setInappropriate(@PathVariable Long id, @PathVariable boolean inappropriate, HttpServletRequest request) {

        User user = getUserFromSession(request);

        if (inappropriate) giftService.setInappropriate(id, user);

        Gift gift = giftService.read(id);

        return new ResponseEntity<GiftResource>(GiftResource.Assembler.toResource(gift), OK);
    }


    /**
     * @return The URL where the photo is uploaded to.
     */
    @RequestMapping(value = "/uploadUrl", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadUrlResource> getUploadUrl(HttpServletRequest request) {

        String uploadUrl = blobstoreService.createUploadUrl("/api/gifts/actions/upload");

        return new ResponseEntity<UploadUrlResource>(new UploadUrlResource(uploadUrl), OK);

    }

    /**
     * Upload Gift.
     */
    @RequestMapping(value = "/actions/upload", method = POST, consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GiftResource> upload(@RequestParam String title, @RequestParam(required = false) String text, @RequestParam(required = false) Long giftChainId, HttpServletRequest request) {

        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> keys = blobs.get("photo");

        if (keys == null || keys.size() == 0) {
            throw new BadStateException("No image has been uploaded");
        }

        BlobKey blobKey = keys.get(0);

        User user = getUserFromSession(request);

        Gift gift = giftService.create(title, text, giftChainId, blobKey.getKeyString(), user);

        return new ResponseEntity<GiftResource>(GiftResource.Assembler.toResource(gift), OK);
    }


    @RequestMapping(value = "/topgivers", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TopGiverResource>> getTopGivers() {

        List<TopGiver> topGivers = giftService.getTopGivers();

        List<TopGiverResource> topGiversResources = new ArrayList<TopGiverResource>();

        for (TopGiver topGiver : topGivers) {
            topGiversResources.add(TopGiverResource.Assembler.toResource(topGiver));
        }

        return new ResponseEntity<List<TopGiverResource>>((topGiversResources), OK);
    }

    @RequestMapping(value = "/actions/search", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GiftResource>> search(@RequestParam String title, HttpServletRequest request) {

        User user = getUserFromSession(request);

        List<Gift> gifts = giftService.search(title, user.doNotShowInappropriate);

        List<GiftResource> giftsResources = new ArrayList<GiftResource>();

        for (Gift gift : gifts) {
            giftsResources.add(GiftResource.Assembler.toResource(gift));
        }

        return new ResponseEntity<List<GiftResource>>(giftsResources, OK);
    }
}
