package com.netgrif.etask.petrinet.web.requestbodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UriCountRequest {

    /**
     * [node.name: node.id]
     */
    Map<String, String> legacyQueries;

    /**
     * [node.uriPath: [menuItemIdentifier1, menuItemIdentifier2]]
     */
    Map<String, List<String>> menuItemIdentifiersQueries;
}
