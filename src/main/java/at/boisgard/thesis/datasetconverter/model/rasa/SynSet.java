/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.rasa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author BUERO
 */
@AllArgsConstructor
public @Data
class SynSet {

    @JsonProperty
    public String value;

    @JsonProperty
    public String[] synonyms;
}
