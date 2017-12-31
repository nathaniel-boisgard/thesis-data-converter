/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.converter;

import at.boisgard.thesis.datasetconverter.model.NamedEntity;
import at.boisgard.thesis.datasetconverter.model.Utterance;
import at.boisgard.thesis.datasetconverter.model.rasa.Entity;
import at.boisgard.thesis.datasetconverter.model.rasa.Intent;
import at.boisgard.thesis.datasetconverter.model.rasa.RasaDataWrapper;
import at.boisgard.thesis.datasetconverter.model.rasa.RasaNluData;
import at.boisgard.thesis.datasetconverter.model.rasa.SynSet;
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
public class RasaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RasaConverter.class);
    public ArrayList<Utterance> utterances;
    public ArrayList<SynSet> synSets = new ArrayList<>();
    public String language;

    /**
     * Init with base Utterances
     *
     * @param utterances
     */
    public RasaConverter(ArrayList<Utterance> utterances, String language) {

        this.utterances = utterances;
        this.language = language;
    }

    /**
     * Convert the base Utterances and save files
     *
     * @return Number of files written
     */
    public void convert() throws IOException {

        // FIRST CREATE UTTERANCE LIST
        ArrayList<Intent> intents = convertUtterances();

        // BUILD DATA FORMAT FRAME
        RasaNluData rasaNLUData = new RasaNluData(intents, new ArrayList(), synSets);
        RasaDataWrapper rasaDataWrapper = new RasaDataWrapper(rasaNLUData);

        saveToFile(rasaDataWrapper);
    }

    /**
     * Convert the base Utterance POJOs to rasa Intents
     *
     * @return List of LUIS Intents
     */
    public ArrayList<Intent> convertUtterances() {

        ArrayList<Intent> results = new ArrayList<>();

        // CREATE INTENT FOR FOR EACH UTTERANCE AND ADD TO LIST
        for (Utterance u : utterances) {

            results.add(new Intent(u.getText(), u.getIntent().getValue(), convertNamedEntities(u.getNamedEntities())));

            // CHECK FOR SYNSETS
            convertNamedEntitiesToSynSets(u.getNamedEntities());
        }

        return results;
    }

    /**
     * Convert base NamedEntity POJOs to rasa Entities
     *
     * @param namesEntites
     * @return List of LUIS Entities
     */
    public ArrayList<Entity> convertNamedEntities(ArrayList<NamedEntity> namesEntites) {

        ArrayList<Entity> results = new ArrayList<>();

        // CREATE ENTITIES FOR EACH NAMED ENTITIES AND ADD TO LIST
        for (NamedEntity nE : namesEntites) {

            results.add(new Entity(nE.getStartAt(), nE.getEndAt(), nE.getName(), nE.getEntityType().getValue()));
        }

        return results;
    }

    /**
     * Take list of NamedEntities and add them to the global list of SynSets
     *
     * @param namesEntites
     */
    public void convertNamedEntitiesToSynSets(ArrayList<NamedEntity> namesEntites) {

        // CHECK FOR ALL ENTITIES IF SYNSET EXISTS
        for (NamedEntity nE : namesEntites) {

            if (nE.getSynonyms().length > 1) {

                // CHECK IF SYNSET IS ALREADY IN LIST, IF NOT -> ADD
                SynSet nSS = new SynSet(nE.getName(), nE.getSynonyms());

                if (synSets.indexOf(nSS) == -1) {

                    synSets.add(nSS);
                }
            }
        }
    }

    /**
     * Save rasa data to disk
     *
     * @param rasaDataWrapper
     * @throws IOException
     */
    private void saveToFile(RasaDataWrapper rasaDataWrapper) throws IOException {

        ObjectMapper oMapper = new ObjectMapper();

        oMapper.writeValue(new File("data/" + language + "/rasa/rasa-training.json"), rasaDataWrapper);
    }
}
