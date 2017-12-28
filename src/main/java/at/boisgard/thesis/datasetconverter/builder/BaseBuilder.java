/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.builder;

import at.boisgard.thesis.datasetconverter.model.EntityType;
import at.boisgard.thesis.datasetconverter.model.Intent;
import at.boisgard.thesis.datasetconverter.model.NamedEntity;
import at.boisgard.thesis.datasetconverter.model.Utterance;
import at.boisgard.thesis.datasetconverter.model.csv.Base;
import at.boisgard.thesis.datasetconverter.model.csv.Competition;
import at.boisgard.thesis.datasetconverter.model.csv.Pattern;
import at.boisgard.thesis.datasetconverter.model.csv.Player;
import at.boisgard.thesis.datasetconverter.model.csv.Team;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
@Component
public class BaseBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseBuilder.class);
    public static final String REGEX_PATTERN = "(\\[[A-Z]+\\])";
    
    public final java.util.regex.Pattern regexPattern;
    
    private final String patternCsvLocation;
    private final String competitionCsvLocation;
    private final String teamCsvLocation;
    private final String playerCsvLocation;
    
    public final String language;
    
    private ArrayList<Pattern> patterns = new ArrayList<>();
    private ArrayList<Competition> competitions = new ArrayList<>();
    private ArrayList<Team> teams = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    
    /**
     * These properties are used to create permutations using all possible labels, including synonyms
     */
    private ArrayList<Competition> competitionLabels = new ArrayList<>();
    private ArrayList<Team> teamLabels = new ArrayList<>();
    
    public ArrayList<Utterance> utterances = new ArrayList<>();
    public ArrayList<Utterance> utterancesIncludingSynonyms = new ArrayList<>();
    
    // LOAD CSVS, CONVERT THEM TO PREFILLED UTTERANCES
    public BaseBuilder(
            @Value("${data.csv.patterns.filepath:'FAIL'}") String patternCsvLocation,
            @Value("${data.csv.competitions.filepath:'FAIL'}") String competitionCsvLocation,
            @Value("${data.csv.teams.filepath:'FAIL'}") String teamCsvLocation,
            @Value("${data.csv.players.filepath:'FAIL'}") String playerCsvLocation,
            @Value("${data.csv.language:'de'}") String language) throws IOException{
        
        regexPattern = java.util.regex.Pattern.compile(REGEX_PATTERN);
        
        this.patternCsvLocation = patternCsvLocation;
        this.competitionCsvLocation = competitionCsvLocation;
        this.teamCsvLocation = teamCsvLocation;
        this.playerCsvLocation = playerCsvLocation;
        this.language = language;
        
        init();
    }
    
    public void setUtterances(ArrayList<Utterance> utterances){
        
        this.utterances = utterances;
    }
    
    public ArrayList<Utterance> getUtterances(){
        
        return this.utterances;
    }

    public ArrayList<Utterance> getUtterancesIncludingSynonyms() {
        
        return utterancesIncludingSynonyms;
    }

    public void setUtterancesIncludingSynonyms(ArrayList<Utterance> utterancesIncludingSynonyms) {
        
        this.utterancesIncludingSynonyms = utterancesIncludingSynonyms;
    }
    
    /**
     * Prepare all fields. Load data from CSVs, create permutations.
     * 
     * @throws IOException 
     */
    private void init() throws IOException {

        patterns = parseCsvFile(patternCsvLocation, Pattern.class);
        competitions = parseCsvFile(competitionCsvLocation, Competition.class);
        teams = parseCsvFile(teamCsvLocation, Team.class);
        players = parseCsvFile(playerCsvLocation, Player.class);
        
        // CREATE LABEL LISTS OUT OF NORMAL ENTITY LISTS
        for(Team t:teams){
            
            teamLabels.add(t);
            
            if(t.getSynonyms().length > 0){
                
                for(String s:t.getSynonyms()){
                    
                    if(!s.equals("")){
                        
                        Team tSyn = new Team(s, t.getSynonymsString());
                        teamLabels.add(tSyn);
                    }                        
                }
            }
        }        
        for(Competition c:competitions){
            
            competitionLabels.add(c);
            
            if(c.getSynonyms().length > 0){
                
                for(String s:c.getSynonyms()){
                    
                    if(!s.equals("")){
                        
                        Competition cSyn = new Competition(s, c.getSynonymsString());
                        competitionLabels.add(cSyn);
                    }                        
                }
            }
        }
        
        setUtterances(createUtterances(false));
        LOGGER.info("Created {} utterances",getUtterances().size());
        
        setUtterancesIncludingSynonyms(createUtterances(true));        
        LOGGER.info("Created {} utterances including entity synonyms",getUtterancesIncludingSynonyms().size());
    }
    
    /**
     * Generic method to load the contents of CSV files into lists of custom POJOs
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

        LOGGER.info("Parsed {} items of type {}", results.size(), clazz.getCanonicalName());

        return (results);
    }
    
    /**
     * Create all Utterances possible with the initialized set of patterns and their replacements
     * 
     * @param withSynonyms
     * @return All possible combinations
     */
    public ArrayList<Utterance> createUtterances(boolean withSynonyms){
        
        ArrayList<Utterance> allUtterances = new ArrayList<>();
        
        // FOR EVERY PATTERN, REPLACE ALL PLACEHOLDERS WITH ALL POSSIBLE CANDIDATES
        for(Pattern pattern:this.patterns){
            
            // THIS IS FOR ALL PERMUTATIONS FOR THIS PATTERN
            ArrayList<Utterance> utterances = new ArrayList<>();
            
            LOGGER.debug("Parsing pattern {} of {}",patterns.indexOf(pattern)+1, patterns.size());
            
            // CREATING BASIC UTTERANCE OUT OF PATTERN
            Utterance newUtterance = new Utterance(pattern.getPattern(), Intent.getFittingIntent(pattern.getIntent()), new ArrayList<>());
            
            // ADD THIS UTTERANCE AS SEED TO THE LIST
            utterances.add(newUtterance);
            
            // KEEP REPLACING PLACEHOLDERS UNTIL NONE ARE LEFT
            do{
                
                // CREATE PERMUTATIONS
                utterances = createPermutationsForPlaceholders(utterances, withSynonyms);
                
            }while(hasPlaceholders(utterances));
            
            // ADD ALL PERMUTATIONS CREATED FROM THIS PATTERN TO THE COMPLETE LIST
            allUtterances.addAll(utterances);
        }
        
        return allUtterances;
    }
    
    /**
     * Create all possible permutations possible for a list of utterances
     * 
     * @param utterances
     * @param withSynonyms
     * @return All possible permutations for given list
     */
    public ArrayList<Utterance> createPermutationsForPlaceholders(ArrayList<Utterance> utterances, boolean withSynonyms){
        
        ArrayList<Utterance> permutations = new ArrayList<>();
        
        // FOR ALL UTTERANCES GIVEN, REPLACE PLACEHOLDERS
        for(Utterance u:utterances){
            
            Matcher m = regexPattern.matcher(u.getText());
            
            if(m.find()){
                
                LOGGER.debug("Placeholder '{}' found. Creating permutations...",m.group());
                
                switch(m.group()){
                    
                    // COMPETITION PLACEHOLDER FOUND
                    case "[COMP]":

                        permutations.addAll(enrichPlaceholderWithEntites(u, withSynonyms?competitionLabels:competitions, m.start(), "COMP"));
                        break;

                    // TEAM PLACEHOLDER FOUND
                    case "[TEAM]":

                        permutations.addAll(enrichPlaceholderWithEntites(u, withSynonyms?teamLabels:teams, m.start(), "TEAM"));
                        break;

                    // PLAYER PLACEHOLDER FOUND
                    case "[PLAY]":

                        permutations.addAll(enrichPlaceholderWithEntites(u, players, m.start(), "PLAY"));
                        break;
                
                }
            }
             
        }
                
        return permutations;
    }
    
    /**
     * Generic method to replace placeholders with elements loaded from CSV files
     * 
     * @param <T>
     * @param u
     * @param container
     * @param startOfPlaceholder
     * @param placeholder
     * @return 
     */
    public <T extends Base> ArrayList<Utterance> enrichPlaceholderWithEntites(Utterance u, ArrayList<T> container, int startOfPlaceholder, String placeholder){
        
        ArrayList<Utterance> permutations = new ArrayList<>();

        for (T item : container) {

            // CLONE OLD ENTITY
            Utterance newUtterance= u.clone();

            // UPDATE OLD TEXT, REPLACE PLACEHOLDER WITH NAME
            newUtterance.setText(newUtterance.getText().replaceFirst("\\[" + placeholder + "\\]", item.getName()));
            
            // CREATE NAMED ENTITY FROM ITEM
            NamedEntity nE = new NamedEntity(
                    item.getName(), 
                    EntityType.getFittingEntityType(placeholder), 
                    startOfPlaceholder, 
                    startOfPlaceholder + item.getName().length(),
                    item.getSynonyms());
            
            // ADD IT TO ENTITY LIST OF UTTERANCE
            newUtterance.getNamedEntities().add(nE);

            // ADD UTTERANCE TO PERMUTATIONS LIST
            permutations.add(newUtterance);
        }
        
        // RETURN PERMUTATIONS
        return permutations;
    } 
    
    /**
     * Check if list of utterances has any placeholders in them
     * 
     * @param utterances
     * @return True if placeholder is found, False otherwise
     */
    public boolean hasPlaceholders(ArrayList<Utterance> utterances){
        
        for(Utterance u: utterances){
            
            Matcher m = regexPattern.matcher(u.getText());
            
            if(m.find()){
                return true;
            }
        }
        
        return false;
    }
}
