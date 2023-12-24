package com.netgrif.etask.petrinet.responsebodies;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.etask.petrinet.web.EtaskUriController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class EtaskUriNodeResource extends EntityModel<EtaskUriNode> {

    public EtaskUriNodeResource(EtaskUriNode content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        UriNode content = getContent();
        if (content != null) {
            add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                            .methodOn(EtaskUriController.class).getRoot())
                    .withSelfRel());

            add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
                            .methodOn(EtaskUriController.class).getOne(content.getUriPath()))
                    .withSelfRel());
        }
    }
}
