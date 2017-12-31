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
@JsonPropertyOrder({"name", "synonymsString"})
@AllArgsConstructor
public @Data
class Team extends Base {

    @JsonProperty
    public String name;
    @JsonProperty
    public String synonymsString;

    @Override
    public String[] getSynonyms() {

        if (synonymsString == null) {
            synonymsString = "";
        }
        return synonymsString.split(";");
    }
}
