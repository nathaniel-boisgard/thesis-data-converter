/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.converter;

import at.boisgard.thesis.datasetconverter.model.NamedEntity;
import at.boisgard.thesis.datasetconverter.model.Utterance;
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
public class CoreNLPConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreNLPConverter.class);
    public ArrayList<Utterance> utterances;
    public String language;

    /**
     * Init with base Utterances
     *
     * @param utterances
     */
    public CoreNLPConverter(ArrayList<Utterance> utterances, String language) {

        this.utterances = utterances;
        this.language = language;
    }

    /**
     * Convert the base Utterances and save files
     *
     * @return Number of files written
     */
    public void convert() throws IOException {

        // TAG ALL NAMED ENTITY MENTIONS
        this.utterances = tagNamedEntityMentions();

        LOGGER.info("Done tagging Named Entity mentions");

        // CREATE FINAL FORMAT
        BufferedWriter bW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/" + language + "/corenlp/corenlp-ner-training.tsv"), StandardCharsets.UTF_8));

        // APPEND CONVERTED UTTERANCE TO FILE
        for (Utterance u : utterances) {

            //LOGGER.info("Converted Uttereance to {}",convertUtteranceToCoreNLPNERFormat(u));
            bW.append(convertUtteranceToCoreNLPNERFormat(u));
        }

        // CLOSE FILE AND BE DONE WITH IT!
        bW.close();
    }

    public ArrayList<Utterance> tagNamedEntityMentions() {

        ArrayList<Utterance> results = new ArrayList<>();

        for (Utterance u : this.utterances) {

            // FOR ALL NAMED ENTITES, REPLACE MENTIONS WITH TAGGED VERSIONS
            for (NamedEntity nE : u.getNamedEntities()) {

                // COLLECT ALL POSSIBLE LABELS
                ArrayList<String> possibleLabels = new ArrayList<>();
                possibleLabels.add(nE.getName());
                for (String s : nE.getSynonyms()) {

                    if (!s.equals("")) {

                        possibleLabels.add(s);
                    }
                }

                // TRY REPLACING ALL POSSIBLE LABELS WITH THEIR ENRICHED VERSIONS
                for (String s : possibleLabels) {

                    u.setText(u.getText().replaceAll("(" + s + ")[^\\tA-Za-z]", enrichNamedEntityMentionWithTags(s, nE.getEntityType().getValue()) + " "));
                }
            }

            results.add(u);
        }

        return results;
    }

    public String enrichNamedEntityMentionWithTags(String mention, String type) {

        StringBuilder result = new StringBuilder();
        String[] stringParts = mention.split(" ");

        int i = 0;
        for (String stringPart : stringParts) {

            String tag = (i == 0) ? "B" : "I";
            result.append(stringPart).append("\t").append(tag).append("-").append(type);
            if (i < stringParts.length - 1) {
                result.append(" ");
            }
            i++;
        }

        return result.toString();
    }

    public String convertUtteranceToCoreNLPNERFormat(Utterance u) {

        // PREPARE END SYMBOLS LIKE QUESTION MARKS AND POINTS
        u.setText(u.getText().replaceAll("([\\.\\;\\?\\!])$", " $1"));

        String[] stringParts = u.getText().split(" ");

        StringBuilder sB = new StringBuilder();

        for (String stringPart : stringParts) {

            sB.append(stringPart);
            // CHECK IF NO TABSTOP CHAT IS PRESENT (IF SO, IT WOULD BE ALREADY ANNOTATED!)
            if (!stringPart.contains("\t")) {
                // APPEND TAG
                sB.append("\tO");
            }
            sB.append("\n");
        }

        // APPEND LINEBREAK AT END OF EXAMPLE
        sB.append("\n");

        return sB.toString();
    }
}
