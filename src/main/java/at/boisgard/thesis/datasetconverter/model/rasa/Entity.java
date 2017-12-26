/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.rasa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author Nathaniel Boisgard
 */
public @Data
class Entity {

    @JsonProperty
    final private int start;
    @JsonProperty
    final private int end;
    @JsonProperty
    final private String value;
    @JsonProperty
    final private String entity;
}
