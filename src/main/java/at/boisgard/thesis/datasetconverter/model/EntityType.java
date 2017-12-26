/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model;

/**
 *
 * @author BUERO
 */
public enum EntityType {
    
    COMP("COMP"),
    PLAY("PLAY"),
    TEAM("TEAM");
    
    private final String value;
    
    EntityType(String value){
        this.value = value;
    }
    
    public String getValue(){
        return this.value;
    }
    
    public static EntityType getFittingEntityType(String entityString){
        
        switch(entityString){
            
            case "COMP":
                return COMP;
            case "PLAY":
                return PLAY;
            case "TEAM":
                return TEAM;
            default:
                return COMP;
        }
    }
}
