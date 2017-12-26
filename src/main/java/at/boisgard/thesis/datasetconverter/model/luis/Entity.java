/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.luis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author Nathaniel Boisgard
 */
public @Data
class Entity {

    @JsonProperty("startCharIndex")
    final private int start;
    @JsonProperty("endCharIndex")
    final private int end;
    @JsonIgnore
    final private String value;
    @JsonProperty("entityName")
    final private String entity;
}
