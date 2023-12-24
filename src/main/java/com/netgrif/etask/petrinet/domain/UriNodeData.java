package com.netgrif.etask.petrinet.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Data
@Document
@NoArgsConstructor
public class UriNodeData {

    @Id
    private ObjectId id;

    @Indexed
    private String uriNodeId;

    private String icon;
    private String section;
    private boolean isIconSvg = false;
    private boolean isHidden = false;
    private Set<String> processRolesIds;
    private List<String> menuItemIdentifiers;

    public UriNodeData(String uriNodeId, String section, String icon, boolean isIconSvg, boolean isHidden, Set<String> processRolesIds, List<String> menuItemIdentifiers) {
        this.uriNodeId = uriNodeId;
        this.icon = icon;
        this.section = section;
        this.isIconSvg = isIconSvg;
        this.isHidden = isHidden;
        this.processRolesIds = processRolesIds;
        this.menuItemIdentifiers = menuItemIdentifiers;
    }
}
