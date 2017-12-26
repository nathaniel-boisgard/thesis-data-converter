/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model;

import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author BUERO
 */

@AllArgsConstructor
public @Data class Utterance {
    
    public String text;
    public Intent intent;
    public ArrayList<NamedEntity> namedEntities;
    
    public Utterance clone(){
        
        ArrayList<NamedEntity> clonedNamedEntities = new ArrayList<>();
        for(NamedEntity nE: this.getNamedEntities()){
            clonedNamedEntities.add(nE);
        }
        
        Utterance clonedUtterance = new Utterance(this.getText(), this.getIntent(), clonedNamedEntities);
        
        return clonedUtterance;
    }
    
}
