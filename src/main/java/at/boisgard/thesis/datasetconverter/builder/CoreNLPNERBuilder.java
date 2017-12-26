/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.builder;

import at.boisgard.thesis.datasetconverter.model.rasa.Entity;
import at.boisgard.thesis.datasetconverter.model.rasa.Intent;
import at.boisgard.thesis.datasetconverter.model.csv.Base;
import at.boisgard.thesis.datasetconverter.model.csv.Pattern;
import at.boisgard.thesis.datasetconverter.model.csv.Competition;
import at.boisgard.thesis.datasetconverter.model.csv.Team;
import at.boisgard.thesis.datasetconverter.model.csv.Player;
import at.boisgard.thesis.datasetconverter.model.rasa.RasaDataWrapper;
import at.boisgard.thesis.datasetconverter.model.rasa.RasaNluData;
import at.boisgard.thesis.datasetconverter.model.rasa.SynSet;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author BUERO
 */

public class CoreNLPNERBuilder {

    public static final String REGEX_PATTERN = "(\\[[A-Z]+\\])";
    private static final Logger logger = LoggerFactory.getLogger(CoreNLPNERBuilder.class);

    private String patternCsvLocation;
    private String competitionCsvLocation;
    private String teamCsvLocation;
    private String playerCsvLocation;

    private ArrayList<Pattern> patterns = new ArrayList<>();
    private ArrayList<Competition> competitions = new ArrayList<>();
    private ArrayList<Team> teams = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();

    private ArrayList<Pattern> replacedPatterns = new ArrayList<>();

    private java.util.regex.Pattern regexPattern;

    /**
     * Map contents of CSV files onto POJOs
     *
     * @param patternCsvLocation
     * @param competitionCsvLocation
     * @param teamCsvLocation
     * @param playerCsvLocation
     * @param trainingTestSplit
     */
    public CoreNLPNERBuilder(
            @Value("${data.csv.patterns.filepath:'FAIL'}") String patternCsvLocation,
            @Value("${data.csv.competitions.filepath:'FAIL'}") String competitionCsvLocation,
            @Value("${data.csv.teams.filepath:'FAIL'}") String teamCsvLocation,
            @Value("${data.csv.players.filepath:'FAIL'}") String playerCsvLocation,
            @Value("${rasa.training.test.split:0.7}") double trainingTestSplit) throws IOException {

        regexPattern = java.util.regex.Pattern.compile(REGEX_PATTERN);

        this.patternCsvLocation = patternCsvLocation;
        this.competitionCsvLocation = competitionCsvLocation;
        this.teamCsvLocation = teamCsvLocation;
        this.playerCsvLocation = playerCsvLocation;

        logger.debug("Pattern file location from properties: {}", patternCsvLocation);
        logger.debug("Competition file location from properties: {}", competitionCsvLocation);
        logger.debug("Team file location from properties: {}", teamCsvLocation);
        logger.debug("Player file location from properties: {}", playerCsvLocation);

        init();

        ArrayList<Intent> seedList = new ArrayList<>();
        for (Pattern p : patterns) {

            seedList.add(Intent.fromPattern(p));
        }

        ArrayList<Intent> intents = new ArrayList<>();

        for (Intent i : seedList) {

            logger.info("Creating data for pattern {}/{}", seedList.indexOf(i), seedList.size());

            ArrayList<Intent> tempWrapper = new ArrayList<>();
            tempWrapper.add(i);

            intents.addAll(createIntentsByReplacingPlaceholders(tempWrapper));       
        }
            
        // WRITE TO TRAINING FILE
        logger.info("Writing to training file for CoreNLP NER");
        BufferedWriter bW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("corenlp-ner-training.tsv"),StandardCharsets.UTF_8));
        
        for(Intent i: intents){
             
            bW.append(i.getText());
            bW.append("\n");
        }    
    }

    private void init() {

        try {

            patterns = parseCsvFile(patternCsvLocation, Pattern.class);
            competitions = parseCsvFile(competitionCsvLocation, Competition.class);
            teams = parseCsvFile(teamCsvLocation, Team.class);
            players = parseCsvFile(playerCsvLocation, Player.class);
            
            // PREPARE AND TAG PATTERNS
            for(Pattern p: patterns){
                
                String textPattern = p.getPattern();
                StringBuilder sB = new StringBuilder();
                String endSymbol = textPattern.substring(textPattern.length()-1);
                
                String cleanedPattern;
                
                // CHECK END SYMBOL
                if(endSymbol.equals("?") || endSymbol.equals("!") || endSymbol.equals(".")){
                    
                    cleanedPattern = textPattern.substring(0, textPattern.length()-1)+" "+endSymbol;
                }else{
                    
                    cleanedPattern = textPattern;
                }
                
                String[] words = cleanedPattern.split(" ");
                
                for(String w: words){
                    
                    sB.append(w);
                    if(!w.contains("[")){
                        sB.append("\tO");
                    }
                    sB.append("\n");
                }
                
                p.setPattern(sB.toString());
            }
            
            // ADD B AND I TAGS TO ARRAYS
            for(Competition item: competitions){
                
                String name = item.getName();
                StringBuilder sB = new StringBuilder();
                
                String[] parts = name.split(" ");
                
                for(int i = 0;i<parts.length;i++){
                    
                    if(i<1){
                        
                        sB.append(parts[i]);
                        sB.append("\tB-COMP");
                        // LINEBREAK IF NOT LAST WORD
                        if(parts.length>1){
                            sB.append("\n");
                        }
                    }else{
                        
                        sB.append(parts[i]);
                        sB.append("\tI-COMP");
                        // LINEBREAK IF NOT LAST WORD
                        if(parts.length>2 && i<(parts.length-1)){
                            sB.append("\n");
                        }
                    }
                }
                
                item.setName(sB.toString());
            }
            for(Player item: players){
                
                String name = item.getName();
                StringBuilder sB = new StringBuilder();
                
                String[] parts = name.split(" ");
                
                for(int i = 0;i<parts.length;i++){
                    
                    if(i<1){
                        
                        sB.append(parts[i]);
                        sB.append("\tB-PLAY");
                        // LINEBREAK IF NOT LAST WORD
                        if(parts.length>1){
                            sB.append("\n");
                        }
                    }else{
                                                
                        sB.append(parts[i]);
                        sB.append("\tI-PLAY");
                        // LINEBREAK IF NOT LAST WORD
                        if(parts.length>2 && i<(parts.length-1)){
                            sB.append("\n");
                        }
                    }
                }
                
                item.setName(sB.toString());
            }
            for(Team item: teams){
                
                String name = item.getName();
                StringBuilder sB = new StringBuilder();
                
                String[] parts = name.split(" ");
                
                for(int i = 0;i<parts.length;i++){
                    
                    if(i<1){
                        
                        sB.append(parts[i]);
                        sB.append("\tB-TEAM");
                        // LINEBREAK IF NOT LAST WORD
                        if(parts.length>1){
                            sB.append("\n");
                        }
                    }else{
                        
                        sB.append(parts[i]);
                        sB.append("\tI-TEAM");
                        // LINEBREAK IF NOT LAST WORD
                        if(parts.length>2 && i<(parts.length-1)){
                            sB.append("\n");
                        }
                    }
                }
                
                item.setName(sB.toString());
            }
            

        } catch (IOException ex) {

            logger.error(ex.toString());
        }
    }

    private ArrayList<Intent> createIntentsByReplacingPlaceholders(ArrayList<Intent> input) {

        // ADD PRELIMINARY TAGS HERE
        // ADD O TO ALL WORDS PRIOR TO REPLACEMENT
        // ADD B AND I + ENTITY TAGS TO REPLACEMENTS STRINGS?
        // 
        
        logger.debug("Parsing iteration started for {} intents.", input.size());

        ArrayList<Intent> newIntentList = new ArrayList<>();

        for (Intent i : input) {

            logger.debug("Parsing intent item {}: {}.", input.indexOf(i), i.getText());

            // CHECK IF PLACEHOLDER IS THERE            
            Matcher m = regexPattern.matcher(i.getText());

            // TAKE FIRST I FIND
            if (m.find()) {

                logger.debug("Placeholder {} found.", m.group());

                // CHECK WHICH TYPE
                switch (m.group()) {

                    // COMPETITION PLACEHOLDER FOUND
                    case "[COMP]":

                        //newIntentList.addAll(enrichIntentWithCompetitions(item, m.start()));
                        newIntentList.addAll(enrichIntentWithEntity(i, competitions, m.start(), "COMP"));
                        break;

                    // TEAM PLACEHOLDER FOUND
                    case "[TEAM]":

                        newIntentList.addAll(enrichIntentWithEntity(i, teams, m.start(), "TEAM"));
                        break;

                    // PLAYER PLACEHOLDER FOUND
                    case "[PLAY]":

                        newIntentList.addAll(enrichIntentWithEntity(i, players, m.start(), "PLAY"));
                        break;
                }

                logger.debug("Placeholder replacement done! Number of intents created: {}", newIntentList.size());
                // CHECK IF MORE PLACEHOLDERS ARE PRESENT IN THE NEWLY CREATED!                
                newIntentList = createIntentsByReplacingPlaceholders(newIntentList);

            } else {

                logger.debug("No more placeholders found! Exiting recursion...");
                // NO PLACEHOLDERS FOUND! WE ARE DONE!
                newIntentList = input;
            }
        }

        logger.debug("Returning intent list with {} items.", newIntentList.size());

        return newIntentList;
    }

    public <T extends Base> ArrayList<Intent> enrichIntentWithEntity(Intent i, ArrayList<T> container, int startOfPlaceholder, String placeholder) {

        ArrayList<Intent> results = new ArrayList<>();

        for (T item : container) {

            // CLONE OLD ENTITY
            Intent newIntent = i.clone();

            // UPDATE OLD TEXT, REPLACE PLACEHOLDER WITH NAME
            newIntent.setText(newIntent.getText().replaceFirst("\\[" + placeholder + "\\]", item.getName()));

            // CREATE ENTITY AND ADD IT
            Entity e = new Entity(startOfPlaceholder, startOfPlaceholder + item.getName().length(), item.getName(), placeholder);
            newIntent.getEntities().add(e);

            results.add(newIntent);
        }

        return results;
    }

    public ArrayList<Intent> getIntents() {

        return null;
    }

    /**
     * Generic method returning an ArrayList containing items of a given class,
     * parsed from a CSV file
     *
     * @param <T>
     * @param path
     * @param clazz
     * @return
     * @throws IOException
     */
    public <T> ArrayList<T> parseCsvFile(String path, Class<T> clazz) throws IOException {

        InputStream iStream = new FileInputStream(path);
        BufferedReader iReader = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));

        MappingIterator<T> items = new CsvMapper().readerWithTypedSchemaFor(clazz).readValues(iReader);
        ArrayList<T> results = (ArrayList) items.readAll();

        logger.info("Parsed {} items of type {}", results.size(), clazz.getCanonicalName());

        return (results);
    }
}
