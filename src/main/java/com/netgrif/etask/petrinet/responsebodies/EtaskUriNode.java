package com.netgrif.etask.petrinet.responsebodies;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

public class EtaskUriNode extends UriNode {

    @Getter
    @Setter
    protected Set<String> roleIds;

    @Getter
    @Setter
    protected List<String> menuItemIdentifiers;

    @Getter
    @Setter
    protected String icon;

    @Getter
    @Setter
    protected String section;

    @Getter
    @Setter
    protected boolean isIconSvg = false;

    @Getter
    @Setter
    protected boolean isHidden = false;

    public EtaskUriNode(UriNode node) {
        super(node.getId(), node.getUriPath(), node.getName(), node.getParentId(), node.getParent(), node.getChildrenId(), node.getChildren(), node.getLevel(), node.getContentTypes());
    }
}
