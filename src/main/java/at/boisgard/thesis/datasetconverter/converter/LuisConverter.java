/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.converter;

import at.boisgard.thesis.datasetconverter.model.NamedEntity;
import at.boisgard.thesis.datasetconverter.model.Utterance;
import at.boisgard.thesis.datasetconverter.model.luis.Entity;
import at.boisgard.thesis.datasetconverter.model.luis.Intent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author BUERO
 */
public class LuisConverter {  
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LuisConverter.class);
    public  ArrayList<Utterance> utterances;
    
    /**
     * Init with base Utterances
     * 
     * @param utterances 
     */
    public LuisConverter(ArrayList<Utterance> utterances){
        
        this.utterances = utterances;
    }
    
    /**
     * Convert the base Utterances and save files
     * 
     * @return Number of files written
     */
    public int convert(){
        
        return saveSplitFiles(convertUtterances());
    }
    
    /**
     * Convert the base Utterance POJOs to LUIS Intents
     * 
     * @return List of LUIS Intents
     */
    public ArrayList<Intent> convertUtterances(){
        
        ArrayList<Intent> results = new ArrayList<>();
        
        // CREATE INTENT FOR FOR EACH UTTERANCE AND ADD TO LIST
        for(Utterance u:utterances){
            
            results.add(new Intent(u.getText(), u.getIntent().getValue(), convertNamedEntities(u.getNamedEntities())));
        }
        
        // @TODO: ADD INTENTS FOR EVERY SYNONYM? 
        
        return results;
    }
    
    /**
     * Convert base NamedEntity POJOs to LUIS Entities
     * 
     * @param namesEntites
     * @return List of LUIS Entities
     */
    public ArrayList<Entity> convertNamedEntities(ArrayList<NamedEntity> namesEntites){
        
        ArrayList<Entity> results = new ArrayList<>();
        
        // CREATE ENTITIES FOR EACH NAMED ENTITIES AND ADD TO LIST
        for(NamedEntity nE: namesEntites){
            
            results.add(new Entity(nE.getStartAt(), nE.getEndAt(), nE.getName(), nE.getEntityType().getValue()));            
        }
        
        return results;
    }
    
    /**
     * Save intents in chunks of 100 (LUIS API only accepts up to 100 intents per call)
     * 
     * @param intents
     * @return Number of files written
     */
    public int saveSplitFiles(ArrayList<Intent> intents){
        
        ObjectMapper oMapper = new ObjectMapper();
        
        try {
            
            ArrayList<Intent> chunk = new ArrayList<>();
            
            int j = 1;
            int nOfFiles = 0;
            for(Intent i: intents){
       
                chunk.add(i);
                
                if(j % 100 == 0){
                    
                    // WRITE FILE 
                    saveToFile(oMapper, chunk, "data/luis/luis-training-"+(Integer)(j/100)+".json");
                    nOfFiles++;
                                        
                    chunk = new ArrayList<>();
                }
                
                j++;
            }
            
            // WRITE REMAINING INTENTS IF PRESENT
            if(chunk.size() > 0){
                
                saveToFile(oMapper, chunk, "data/luis/luis-training-"+(Integer)(j/100)+".json");
                nOfFiles++;
            }
            
            return nOfFiles;
            
        } catch (IOException e) {
            
            LOGGER.error(e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Save chunk of LUIS Intents to disk
     * 
     * @param oM
     * @param intents
     * @param fileName
     * @throws IOException 
     */
    private void saveToFile(ObjectMapper oM, ArrayList<Intent> intents, String fileName) throws IOException{
        
        oM.writeValue(new File(fileName), intents);
    }
}
