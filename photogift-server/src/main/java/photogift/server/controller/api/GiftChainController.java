package photogift.server.controller.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import photogift.server.domain.GiftChain;
import photogift.server.domain.User;
import photogift.server.filter.Authenticate;
import photogift.server.resource.GiftChainResource;
import photogift.server.resource.PagedGiftChainAssembler;
import photogift.server.service.GiftChainService;
import photogift.server.service.PagedResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/giftchains")
public class GiftChainController {

    @Autowired
    private GiftChainService giftChainService;

    @RequestMapping(value = "/", produces = APPLICATION_JSON_VALUE, method = GET)
    public String root() {
        return "redirect:pages";
    }

    @RequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<GiftChainResource> readGiftChain(@PathVariable Long id, HttpServletRequest request) {

        User user = Authenticate.getUserFromSession(request);

        boolean doNotShowInappropriate = true;

        if (user != null) {
            doNotShowInappropriate = user.doNotShowInappropriate;
        }

        GiftChain giftChain = giftChainService.read(id, doNotShowInappropriate);

        GiftChainResource resource = GiftChainResource.Assembler.toResource(giftChain);

        return new ResponseEntity<GiftChainResource>(resource, OK);
    }

    /**
     * Read GiftChain List from the lastest position (current)
     */
    @RequestMapping(value = "/pages", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<List<GiftChainResource>> readGiftChains() {
        return readGiftChains(null);
    }


    /**
     * Read the GiftChains newer than this curson position (future)
     */
    @RequestMapping(value = "/pages/{cursor}/newer", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<List<GiftChainResource>> readPreviousGiftChains(@PathVariable String cursor) {

        PagedResult<GiftChain> pagedGiftChain = giftChainService.readPreviousWithPaging(cursor);

        List<GiftChainResource> resources = PagedGiftChainAssembler.toResource(pagedGiftChain);

        return new ResponseEntity<List<GiftChainResource>>(resources, OK);
    }


    /**
     * Read GiftChain List from at cursor (past)
     */
    @RequestMapping(value = "/pages/{cursor}", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResponseEntity<List<GiftChainResource>> readGiftChains(@PathVariable String cursor) {

        PagedResult<GiftChain> pagedGiftChain = giftChainService.readWithPaging(cursor);

        List<GiftChainResource> resources = PagedGiftChainAssembler.toResource(pagedGiftChain);

        return new ResponseEntity<List<GiftChainResource>>(resources, OK);
    }

}
