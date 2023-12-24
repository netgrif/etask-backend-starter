package com.netgrif.etask.startup

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.AbstractOrderedCommandLineRunner
import com.netgrif.application.engine.startup.ImportHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NetRunner extends AbstractOrderedCommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(NetRunner.class)

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Override
    void run(String... args) throws Exception {
        log.info("Calling net runner")
        for (PetriNetEnum netEnum : PetriNetEnum.values()) {
            importNet(netEnum)
        }
    }

    void importNet(PetriNetEnum petriNetEnum, boolean reimport = false) {
        if (petriNetEnum.doImport && (reimport || !petriNetService.getNewestVersionByIdentifier(petriNetEnum.identifier))) {
            helper.createNet(petriNetEnum.file)
        }
    }

    enum PetriNetEnum {

        CONFIGURATION_TILES("configuration_tiles.xml", "configuration_tiles"),


        public final String file
        public final String identifier
        public final boolean doImport

        PetriNetEnum(String file, String identifier) {
            this(file, identifier, true)
        }

        PetriNetEnum(String file, String identifier, boolean doImport) {
            this.file = file
            this.identifier = identifier
            this.doImport = doImport
        }
    }
}
