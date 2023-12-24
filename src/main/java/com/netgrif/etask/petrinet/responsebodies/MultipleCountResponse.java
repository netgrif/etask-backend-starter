package com.netgrif.etask.petrinet.responsebodies;

import lombok.Data;

import java.util.Map;

@Data
public class MultipleCountResponse {

    Map<String,Integer> counts;

    public MultipleCountResponse(Map<String, Integer> counts) {
        this.counts = counts;
    }
}
