package com.netgrif.etask.petrinet.web;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.etask.petrinet.domain.UriNodeDataRepository;
import com.netgrif.etask.petrinet.responsebodies.EtaskUriNode;
import com.netgrif.etask.petrinet.responsebodies.EtaskUriNodeResource;
import com.netgrif.etask.petrinet.responsebodies.EtaskUriNodeResources;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * becomes obsolete in NAE 6.4.0 where double drawer menu is reworked
 */
@RestController
@RequestMapping("/api/v2/uri")
@Tag(name = "Process URI")
public class EtaskUriController {

    private final IUriService uriService;
    private final UriNodeDataRepository repository;

    public EtaskUriController(IUriService uriService, UriNodeDataRepository repository) {
        this.uriService = uriService;
        this.repository = repository;
    }

    @Operation(summary = "Get root UriNodes", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/root", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EtaskUriNode> getRoot() {
        EtaskUriNode uriNode = new EtaskUriNode(uriService.getRoot());
        uriNode = populateDirectRelatives(loadUriNode(uriNode));
        return new EtaskUriNodeResource(uriNode);
    }

    @Operation(summary = "Get one UriNode by URI path", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/{uri}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EtaskUriNode> getOne(@PathVariable("uri") String uri) {
        uri = new String(Base64.getDecoder().decode(uri));
        EtaskUriNode uriNode = new EtaskUriNode(uriService.findByUri(uri));
        uriNode = populateDirectRelatives(loadUriNode(uriNode));
        return new EtaskUriNodeResource(uriNode);
    }

    @Operation(summary = "Get UriNodes by parent id", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/parent/{parentId}", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EtaskUriNode> getByParent(@PathVariable("parentId") String parentId) {
        List<EtaskUriNode> uriNodes = uriService.findAllByParent(parentId).stream().map(this::loadUriNode).collect(Collectors.toList());
        uriNodes.forEach(this::populateDirectRelatives);
        return new EtaskUriNodeResources(uriNodes);
    }

    @Operation(summary = "Get UriNodes by on the same level", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/level/{level}", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EtaskUriNode> getByLevel(@PathVariable("level") int level) {
        List<EtaskUriNode> uriNodes = uriService.findByLevel(level).stream().map(this::loadUriNode).collect(Collectors.toList());
        uriNodes.forEach(this::populateDirectRelatives);
        return new EtaskUriNodeResources(uriNodes);
    }

    protected EtaskUriNode populateDirectRelatives(EtaskUriNode customUriNode) {
        uriService.populateDirectRelatives(customUriNode);
        Set<UriNode> children = customUriNode.getChildren().stream().map(this::loadUriNode).collect(Collectors.toSet());
        customUriNode.setChildren(children);
        return customUriNode;
    }

    protected EtaskUriNode loadUriNode(UriNode node) {
        EtaskUriNode customUriNode = new EtaskUriNode(node);
        repository.findByUriNodeId(node.getId()).ifPresent(data -> {
            customUriNode.setRoleIds(data.getProcessRolesIds());
            customUriNode.setMenuItemIdentifiers(data.getMenuItemIdentifiers());
            customUriNode.setIcon(data.getIcon());
            customUriNode.setIconSvg(data.isIconSvg());
            customUriNode.setSection(data.getSection());
            customUriNode.setHidden(data.isHidden());
        });
        return customUriNode;
    }
}
