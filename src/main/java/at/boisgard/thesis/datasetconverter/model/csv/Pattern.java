/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author BUERO
 */
@JsonPropertyOrder({"pattern", "intent"})
@AllArgsConstructor
public @Data
class Pattern {

    @JsonProperty
    private String pattern;
    @JsonProperty
    private String intent;

}
