/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.converter;

import at.boisgard.thesis.datasetconverter.model.NamedEntity;
import at.boisgard.thesis.datasetconverter.model.Utterance;
import at.boisgard.thesis.datasetconverter.model.watson.Entity;
import at.boisgard.thesis.datasetconverter.model.watson.Intent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author BUERO
 */
public class WatsonConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatsonConverter.class);
    public ArrayList<Utterance> utterances;
    public String language;

    public ArrayList<Intent> intents = new ArrayList<>();
    public ArrayList<Entity> entities = new ArrayList<>();

    /**
     * Init with base Utterances
     *
     * @param utterances
     */
    public WatsonConverter(ArrayList<Utterance> utterances, String language) {

        this.utterances = utterances;
        this.language = language;
    }

    /**
     * Convert the base Utterances and save files
     *
     * @throws java.io.IOException
     */
    public void convert() throws IOException {

        convertUtterances();
        save();
    }

    /**
     * Convert the base Utterance POJOs to Watson Intents *
     *
     */
    public void convertUtterances() {

        for (Utterance u : utterances) {

            Intent iNew = new Intent(u.getText(), u.getIntent().getValue());
            intents.add(iNew);

            // CONVERT ALL NAMED ENTITES
            convertNamedEntitiesToEntities(u.getNamedEntities());
        }
    }

    /**
     * Take list of NamedEntities, convert them, and add them to the global list
     * of Entities
     *
     * @param namedEntites
     */
    public void convertNamedEntitiesToEntities(ArrayList<NamedEntity> namedEntites) {

        for (NamedEntity nE : namedEntites) {

            Entity eNew = new Entity(nE.getName(), nE.getEntityType().getValue(), nE.getSynonyms());

            if (entities.indexOf(eNew) == -1) {

                entities.add(eNew);
            }
        }
    }

    public void save() throws IOException {

        BufferedWriter bWEntities = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/" + language + "/watson/entities.csv"), StandardCharsets.UTF_8));

        // SAVE INTENTS INTO PACKS OF 10000 MAX
        ArrayList<Intent> chunk = new ArrayList<>();

        int j = 1;
        for (Intent i : intents) {

            chunk.add(i);

            if (chunk.size() == 10000) {

                saveIntentChunk(chunk, j);

                j++;
                chunk = new ArrayList<>();
            }
        }

        // SAVE WHATEVER IS LEFT IN THE LAST CHUNK
        saveIntentChunk(chunk, j);

        for (Entity e : entities) {

            bWEntities.append(e.getEntityType()).append(",").append(e.getValue());

            for (String s : e.getSynonyms()) {

                if (!s.equals("")) {

                    bWEntities.append(",").append(s);
                }
            }

            bWEntities.append("\n");
        }

        bWEntities.close();
    }

    public void saveIntentChunk(ArrayList<Intent> chunk, int index) throws IOException {

        BufferedWriter bWIntents = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/" + language + "/watson/intents" + Integer.toString(index) + ".csv"), StandardCharsets.UTF_8));

        for (Intent i : chunk) {

            bWIntents.append(i.getText()).append(",").append(i.getIntent()).append("\n");
        }

        bWIntents.close();
    }
}
