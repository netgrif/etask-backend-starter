package com.netgrif.etask

import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.etask.petrinet.domain.UriNodeData
import com.netgrif.etask.petrinet.domain.UriNodeDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EtaskActionDelegate extends ActionDelegate {

    @Autowired
    private UriNodeDataRepository uriNodeDataRepository

    /**
     * update menu item property
     * @param id
     * @param section to put this item in instead of default (currently only "settings" supported)
     * @return menu item
     */
    Case updateMenuItemSection(String id, String section = "settings") {
        Case menuItem = findMenuItem(id)
        if (!menuItem) {
            return null
        }
        return updateMenuItemSection(menuItem, section)
    }

    Case updateMenuItemSection(Case menuItem, String section = "settings") {
        menuItem.dataSet["custom_drawer_section"].value = section
        return workflowService.save(menuItem)
    }

    /**
     * set roles to uri node based on uriPaths
     * all roles of all processes from provided uriPaths will be able to see the node
     * @param uri
     * @param uriPaths
     */
    void setUriNodeDataRolesByPaths(String uri, List<String> uriPaths) {
        List<PetriNet> nets = uriPaths.collect {
            UriNode node = getUri(it) as UriNode
            if (!node) return null
            return petriNetService.findAllByUri(node.id)
        }.findAll { it != null }.flatten() as List<PetriNet>
        List<String> roleIds = nets.collect { it.roles.keySet() as List }.flatten() as List<String>
        setUriNodeDataRoles(uri, roleIds)
    }

    /**
     * set roles to uri node
     * @param uri
     * @param netRoles [net_identifier: [admin, system, ...]
     */
    void setUriNodeDataRoles(String uri, Map<String, List<String>> netRoles) {
        List<ProcessRole> roles = netRoles.collect { entry ->
            def net = petriNetService.getNewestVersionByIdentifier(entry.key)
            return net.roles.values().findAll { role -> entry.value.any { roleImportId -> roleImportId == role.importId } }
        }.flatten() as List<ProcessRole>
        setUriNodeDataRoles(uri, roles.stringId as List)
    }

    /**
     * set filters to uri node
     * @param uri
     * @param menu item identifiers
     */
    void setUriNodeDataFilters(String uri, List<String> menuItemIdentifiers) {
        UriNode uriNode = getUri(uri) as UriNode
        uriNodeDataRepository.findByUriNodeId(uriNode.getId()).ifPresentOrElse(data -> {
            data.setMenuItemIdentifiers(menuItemIdentifiers)
            uriNodeDataRepository.save(data)
        }, () -> {
            uriNodeDataRepository.save(new UriNodeData(uriNode.getId(), null, null, false, false, null, menuItemIdentifiers))
        })
    }

    /**
     * set roles to uri node for counters
     * @param uri
     * @param roleIds role stringIds
     */
    void setUriNodeDataRoles(String uri, List<String> roleIds) {
        UriNode uriNode = getUri(uri) as UriNode
        uriNodeDataRepository.findByUriNodeId(uriNode.getId()).ifPresentOrElse(data -> {
            data.setProcessRolesIds(roleIds as Set)
            uriNodeDataRepository.save(data)
        }, () -> {
            uriNodeDataRepository.save(new UriNodeData(uriNode.getId(), null, null, false, false, roleIds as Set, null))
        })
    }

    /**
     * set custom uri node data
     * @param uri
     * @param title
     * @param section - "settings" or "archive" if root, else null
     * @param icon
     * @param isSvgIcon
     * @param isHidden
     * @param roleIds - if null, no restriction
     */
    void setUriNodeData(String uri, String title, String section, String icon, boolean isSvgIcon = false, boolean isHidden = false, List<String> roleIds = null) {
        UriNode uriNode = getUri(uri) as UriNode
        uriNode.setName(title)
        uriService.save(uriNode)
        uriNodeDataRepository.findByUriNodeId(uriNode.getId()).ifPresentOrElse(data -> {
            data.setIcon(icon)
            data.setSection(section)
            data.setIconSvg(isSvgIcon)
            data.setProcessRolesIds(roleIds as Set)
            data.setHidden(isHidden)
            uriNodeDataRepository.save(data)
        }, () -> {
            uriNodeDataRepository.save(new UriNodeData(uriNode.getId(), section, icon, isSvgIcon, isHidden, roleIds as Set, null))
        })
    }

    boolean canUserAccessMenuItem(Case menuItem, IUser user) {
        Map<String, I18nString> allowedRoles = menuItem.getDataField("allowed_roles").options
        Map<String, I18nString> bannedRoles = menuItem.getDataField("banned_roles").options
        boolean hasAllowedRole = !allowedRoles || allowedRoles.keySet().any { encoded ->
            def importIdToNet = parseRoleFromMenuItemRoles(encoded)
            def net = petriNetService.getNewestVersionByIdentifier(importIdToNet.values()[0])
            return user.processRoles.any { it.importId == importIdToNet.keySet()[0] && it.netId == net.stringId }
        }
        boolean hasBannedRole = bannedRoles && bannedRoles.keySet().any { encoded ->
            def importIdToNet = parseRoleFromMenuItemRoles(encoded)
            def net = petriNetService.getNewestVersionByIdentifier(importIdToNet.values()[0])
            return user.processRoles.any { it.importId == importIdToNet.keySet()[0] && it.netId == net.stringId }
        }
        return hasAllowedRole && !hasBannedRole
    }

    protected static Map<String, String> parseRoleFromMenuItemRoles(String encoded) {
        def split = encoded.split(":")
        return [(split[0]): split[1]]
    }

    def createNewUser(String name, String surname, String email, String password) {
        if (userService.findByEmail(email, true) != null) {
            throw new IllegalArgumentException("Používateľ s rovnakým emailom už bol vytvorený")
        }
        userService.saveNew(new User(
                name: name,
                surname: surname,
                email: email,
                password: password,
                state: UserState.ACTIVE,
                authorities: [] as Set<Authority>,
                processRoles: [] as Set<ProcessRole>))
    }

}