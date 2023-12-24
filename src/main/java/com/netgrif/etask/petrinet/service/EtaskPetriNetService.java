package com.netgrif.etask.petrinet.service;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Primary
public class EtaskPetriNetService extends PetriNetService {

    private final PetriNetRepository repository;

    public EtaskPetriNetService(PetriNetRepository repository) {
        this.repository = repository;
    }

    /**
     * TODO this only exists because of engine bug
     */
    @Override
    public Optional<PetriNet> save(PetriNet petriNet) {
        petriNet.initializeArcs();

        Optional<PetriNet> net = Optional.of(repository.save(petriNet));
        this.evictAllCaches();
        return net;
    }

    /**
     * does the same as NAE method but uses PetriNet::importXmlPath instead of making the path up
     * @param netId net id
     * @param title unused
     * @return file
     */
    @Override
    public FileSystemResource getFile(String netId, String title) {
        PetriNet net = getPetriNet(netId);
        return new FileSystemResource(net.getImportXmlPath());
    }


}
