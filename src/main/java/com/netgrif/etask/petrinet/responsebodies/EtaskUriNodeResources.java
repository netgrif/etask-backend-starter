package com.netgrif.etask.petrinet.responsebodies;

import com.netgrif.etask.petrinet.web.EtaskUriController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public class EtaskUriNodeResources extends CollectionModel<EtaskUriNode> {

    public EtaskUriNodeResources(Iterable<EtaskUriNode> content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EtaskUriController.class)
                .getRoot()).withRel("root"));
    }
}
