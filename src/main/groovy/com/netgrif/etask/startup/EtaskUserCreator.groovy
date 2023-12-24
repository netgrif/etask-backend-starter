package com.netgrif.etask.startup


import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.startup.AbstractOrderedCommandLineRunner
import com.netgrif.etask.EtaskProperties
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EtaskUserCreator extends AbstractOrderedCommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(EtaskUserCreator.class)

    @Autowired
    private EtaskProperties etaskProperties

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IUserService userService

    @Override
    void run(String... args) throws Exception {
        etaskProperties.users.each { propId, userProp ->
            if (StringUtils.isAnyBlank(userProp.email, userProp.password)) {
                log.info("User $propId from application properties has no email or password")
                return
            }
            def user = userService.findByEmail(userProp.email, true)
            if (user) {
                log.info("User [$propId] ${userProp.email} already exist")
                return
            }
            def name = resolveName(userProp.name)
            user = new User(userProp.email, userProp.password, name[0], name[1])
            user.state = UserState.ACTIVE
            userProp.authorities.collect { authorityService.getOrCreate(it) }.each { user.addAuthority(it) }
            user = userService.saveNew(user)
            log.info("Created new user from properties [$propId] ${user.email} ${user.fullName} with ${userProp.authorities}")
        }
    }

    static String[] resolveName(String value) {
        value = value.trim()
        int spaceIdx = value.indexOf(" ")
        if (spaceIdx == -1) return new String[]{value, ""}
        def name = value.substring(0, spaceIdx)
        def surname = spaceIdx == value.length() - 1 ? "" : value.substring(spaceIdx + 1)
        return new String[]{name, surname}
    }

}
