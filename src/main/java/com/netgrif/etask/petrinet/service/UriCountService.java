package com.netgrif.etask.petrinet.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository;
import com.netgrif.application.engine.startup.FilterRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.etask.EtaskActionDelegate;
import com.netgrif.etask.petrinet.service.interfaces.IUriCountService;
import com.netgrif.etask.petrinet.web.requestbodies.UriCountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UriCountService implements IUriCountService {

    private final UriNodeRepository uriNodeRepository;
    private final EtaskActionDelegate actionDelegate;
    private final IElasticCaseService elasticCaseService;
    private final IWorkflowService workflowService;

    public UriCountService(UriNodeRepository uriNodeRepository, EtaskActionDelegate actionDelegate, IElasticCaseService elasticCaseService, IWorkflowService workflowService) {
        this.uriNodeRepository = uriNodeRepository;
        this.actionDelegate = actionDelegate;
        this.elasticCaseService = elasticCaseService;
        this.workflowService = workflowService;
    }

    @Override
    public Map<String, Integer> count(UriCountRequest request, IUser user, Locale locale) {
        Map<String, Integer> results = new HashMap<>();
        results.putAll(countByLegacyQueries(request.getLegacyQueries(), user, locale));
        results.putAll(countByFilters(request.getMenuItemIdentifiersQueries(), user, locale));
        return results;
    }

    protected Map<String, Integer> countByFilters(Map<String, List<String>> filtersMap, IUser user, Locale locale) {
        Map<String, Integer> results = new HashMap<>();
        filtersMap.forEach((uriPath, menuItemIdentifiers) -> {
            UriNode node = uriNodeRepository.findByUriPath(uriPath);
            if (node == null) {
                return;
            }
            List<Case> menuItems = menuItemIdentifiers.stream().map(it -> actionDelegate.findMenuItem(it)).collect(Collectors.toList());
            List<Case> menuItemsUserHasAccessTo = menuItems.stream().filter(it -> actionDelegate.canUserAccessMenuItem(it, user.getSelfOrImpersonated())).collect(Collectors.toList());
            List<String> filters = menuItemsUserHasAccessTo.stream().map(mi -> (String) actionDelegate.getFilterFromMenuItem(mi).getFieldValue("filter")).collect(Collectors.toList());
            List<CaseSearchRequest> requests = filters.stream().map(filter -> CaseSearchRequest.builder().query(filter).build()).collect(Collectors.toList());
            long count;
            if (requests.isEmpty()) {
                count = 0;
            } else {
                count = elasticCaseService.count(requests, user.transformToLoggedUser(), locale, false);
            }
            results.put(node.getName(), (int) count);
        });
        return results;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Integer> countByLegacyQueries(Map<String, String> queries, IUser user, Locale locale) {
        Map<String, Integer> count = new HashMap<>();
        List<UriNode> allUri = StreamSupport.stream(uriNodeRepository.findAll().spliterator(), false).collect(Collectors.toList());
        allUri.forEach(uriNode -> {
            if (uriNode.getLevel() != 0) {
                UriNode parent = allUri.stream().filter(uri -> Objects.equals(uri.getId(), uriNode.getParentId())).findFirst().orElse(null);
                uriNode.setParent(parent);
            }
            if (uriNode.getChildrenId() != null && !uriNode.getChildrenId().isEmpty()) {
                Set<UriNode> childrens = allUri.stream().filter(uri -> uriNode.getChildrenId().contains(uri.getId())).collect(Collectors.toSet());
                uriNode.setChildren(childrens);
            }
        });

        queries.forEach((key, uriId) -> {
            UriNode uriNode = allUri.stream().filter(uri -> Objects.equals(uri.getId(), uriId)).findFirst().orElse(null);
            if (uriNode != null) {
                List<CaseSearchRequest> filters = resolveUriTree(uriNode).stream().map(uriIdd -> CaseSearchRequest.builder()
                        .uriNodeId(uriIdd)
                        .process(List.of(new CaseSearchRequest.PetriNet(FilterRunner.PREFERRED_FILTER_ITEM_NET_IDENTIFIER)))
                        .build()).collect(Collectors.toList());
                List<Case> filterItems = elasticCaseService.search(filters, user.transformToLoggedUser(), PageRequest.ofSize(10000), locale, false).getContent();
                List<Case> filterss = workflowService.findAllById(filterItems.stream()
                        .map(aCase -> (List<String>) aCase.getDataField("filter_case").getValue())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
                List<CaseSearchRequest> queres = filterss.stream()
                        .map(aCase -> CaseSearchRequest.builder()
                                .query((String) aCase.getDataField("filter").getValue())
                                .build())
                        .collect(Collectors.toList());

                count.put(key, (int) elasticCaseService.count(queres, user.transformToLoggedUser(), locale, false));
            }
        });

        return count;
    }

    private Set<String> resolveUriTree(UriNode uriNode) {
        Set<String> uriNodeIdTree = new HashSet<>();
        if (uriNode.getLevel() != 0) {
            uriNodeIdTree.add(uriNode.getId());
        }
        resolveUriTree(uriNode.getChildren(), uriNodeIdTree);

        return uriNodeIdTree;
    }

    private void resolveUriTree(Set<UriNode> uriNodes, Set<String> uriNodeIdTree) {
        if (uriNodes != null && !uriNodes.isEmpty()) {
            uriNodes.forEach(uriNode -> {
                if (!uriNodeIdTree.contains(uriNode.getId())) {
                    uriNodeIdTree.add(uriNode.getId());
                    resolveUriTree(uriNode.getChildren(), uriNodeIdTree);
                }
            });
        }
    }


}
