package com.netgrif.etask.petrinet.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UriNodeDataRepository extends MongoRepository<UriNodeData, String> {

    Optional<UriNodeData> findByUriNodeId(String uriNodeId);

}
