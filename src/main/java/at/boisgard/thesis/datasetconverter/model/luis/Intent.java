/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.luis;

import at.boisgard.thesis.datasetconverter.model.csv.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Nathaniel Boisgard
 */
@AllArgsConstructor
public @Data
class Intent implements Cloneable {

    @JsonProperty
    private String text;
    @JsonProperty("intentName")
    private String intent;
    @JsonProperty("entityLabels")
    private ArrayList<Entity> entities;

    @Override
    public Intent clone() {

        ArrayList<Entity> newList = new ArrayList<>();

        for (Entity e : entities) {

            newList.add(e);
        }

        return new Intent(text, intent, newList);
    }

    public static Intent fromPattern(Pattern p) {

        return new Intent(p.getPattern(), p.getIntent(), new ArrayList<>());
    }
}
