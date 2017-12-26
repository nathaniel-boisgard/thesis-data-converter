/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.rasa;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author BUERO
 */
@AllArgsConstructor
public @Data
class RasaNluData {

    @JsonProperty
    public ArrayList<Intent> common_examples;
    @JsonProperty
    public ArrayList regex_features;
    @JsonProperty
    public ArrayList<SynSet> entity_synonyms;

}
