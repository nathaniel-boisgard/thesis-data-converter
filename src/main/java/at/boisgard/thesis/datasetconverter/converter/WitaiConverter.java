/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.converter;

import at.boisgard.thesis.datasetconverter.model.NamedEntity;
import at.boisgard.thesis.datasetconverter.model.Utterance;
import at.boisgard.thesis.datasetconverter.model.witai.BaseEntity;
import at.boisgard.thesis.datasetconverter.model.witai.Entity;
import at.boisgard.thesis.datasetconverter.model.witai.Sample;
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
public class WitaiConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WitaiConverter.class);
    public ArrayList<Utterance> utterances;
    public String language;

    /**
     * Init with base Utterances
     *
     * @param utterances
     */
    public WitaiConverter(ArrayList<Utterance> utterances, String language) {

        this.utterances = utterances;
        this.language = language;
    }

    /**
     * Convert the base Utterances and save files
     *
     * @return Number of files written
     */
    public int convert() {

        return saveSplitFiles(convertUtterances());
    }

    /**
     * Convert the base Utterance POJOs to Wit.ai Samples
     *
     * @return List of Wit Samples
     */
    public ArrayList<Sample> convertUtterances() {

        ArrayList<Sample> results = new ArrayList<>();

        // CREATE INTENT FOR FOR EACH UTTERANCE AND ADD TO LIST
        for (Utterance u : utterances) {

            results.add(new Sample(u.getText(), createEntities(u)));
        }

        // @TODO: ADD INTENTS FOR EVERY SYNONYM? 
        return results;
    }

    /**
     * Convert base Utterance to Wit.ai entities
     *
     *
     * @param u
     * @return List of Wit Entities
     */
    public ArrayList<BaseEntity> createEntities(Utterance u) {

        ArrayList<BaseEntity> results = new ArrayList<>();

        // CREATE BASE INTENT ENTITY
        BaseEntity intentEntity = new BaseEntity(u.getIntent().getValue(), "intent");
        results.add(intentEntity);

        // CREATE ENTITIES FOR EACH NAMED ENTITIES AND ADD TO LIST
        for (NamedEntity nE : u.getNamedEntities()) {
            
            results.add(new Entity(nE.getStartAt(), nE.getEndAt(), nE.getName(), nE.getEntityType().getValue()));
        }

        return results;
    }

    /**
     * Save samples in chunks of 100
     *
     * @param samples
     * @return Number of files written
     */
    public int saveSplitFiles(ArrayList<Sample> samples) {

        ObjectMapper oMapper = new ObjectMapper();

        try {

            ArrayList<Sample> chunk = new ArrayList<>();

            int j = 1;
            int nOfFiles = 0;
            for (Sample s : samples) {

                chunk.add(s);

                if (j % 100 == 0) {

                    // WRITE FILE 
                    saveToFile(oMapper, chunk, "data/" + language + "/wit.ai/witai-training-" + (Integer) (j / 100) + ".json");
                    nOfFiles++;

                    chunk = new ArrayList<>();
                }

                j++;
            }

            // WRITE REMAINING INTENTS IF PRESENT
            if (chunk.size() > 0) {

                saveToFile(oMapper, chunk, "data/" + language + "/wit.ai/witai-training-" + (Integer) (j / 100) + ".json");
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
    private void saveToFile(ObjectMapper oM, ArrayList<Sample> intents, String fileName) throws IOException {

        oM.writeValue(new File(fileName), intents);
    }
}
