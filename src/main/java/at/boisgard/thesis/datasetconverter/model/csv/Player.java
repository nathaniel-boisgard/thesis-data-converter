/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author BUERO
 */

public @Data
class Player extends Base {
    
    public Player(String name){
        this.name = name;
    }
    
    public Player(){
        
    }

    @JsonProperty
    public String name;
}
