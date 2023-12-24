package com.netgrif.etask.petrinet.web;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.etask.petrinet.responsebodies.MultipleCountResponse;
import com.netgrif.etask.petrinet.service.interfaces.IUriCountService;
import com.netgrif.etask.petrinet.web.requestbodies.UriCountRequest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v2/uri")
public class UriCounterController {

    private final IUriCountService countService;
    private final IUserService userService;

    public UriCounterController(IUriCountService countService, IUserService userService) {
        this.countService = countService;
        this.userService = userService;
    }

    @PostMapping(value = "/count", produces = MediaTypes.HAL_JSON_VALUE)
    public MultipleCountResponse count(@RequestBody UriCountRequest request, Authentication auth, Locale locale) {
        return new MultipleCountResponse(countService.count(request, userService.getLoggedUser(), locale));
    }
}
