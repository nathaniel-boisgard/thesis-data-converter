/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.witai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author BUERO
 */
public @Data
class Entity extends BaseEntity {

    @JsonProperty
    public int start;

    @JsonProperty
    public int end;

    public Entity(int start, int end, String value, String entity) {

        super(value, entity);

        this.start = start;
        this.end = end;
    }

}
