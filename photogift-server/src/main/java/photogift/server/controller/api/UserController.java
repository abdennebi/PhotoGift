package photogift.server.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import photogift.server.domain.User;
import photogift.server.exception.BadStateException;
import photogift.server.filter.Authenticate;
import photogift.server.resource.UserResource;
import photogift.server.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static photogift.server.resource.UserResource.Assembler.toResource;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;

    /**
     * Exposed as `GET /api/users/me`.
     * <p/>
     * Returns a user resource for the currently authenticated user.
     * <p/>
     * {
     * "id":"",
     * "google_plus_id":"",
     * "google_display_name":"",
     * "google_photo_url":"",
     * "google_profile_url":"",
     * "last_updated":""
     * }
     *
     * @throws IOException if the response fails to fetch its writer
     */

    @RequestMapping(value = "/me", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> doGet(HttpServletRequest request) throws IOException {

        User sessionUser = Authenticate.getUserFromSession(request);
        String googleId = sessionUser.googleUserId;
        // Fetch the most up-to-date version of the User profile
        User user = userService.loadUserWithGoogleId(googleId);

        return new ResponseEntity<UserResource>(toResource(user), OK);
    }

    @RequestMapping(value = "/me", method = POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> update(@RequestParam boolean doNotShowInappropriate, HttpServletRequest request) throws IOException {
        User user = Authenticate.getUserFromSession(request);
        if (user == null) {
            throw new BadStateException("The session is unauthenticated");
        }

        User updatedUser = userService.setDoNotShowInappropriate(user, doNotShowInappropriate);
        return new ResponseEntity<UserResource>(toResource(updatedUser), OK);
    }
}
