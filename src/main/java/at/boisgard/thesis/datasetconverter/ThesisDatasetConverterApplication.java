package at.boisgard.thesis.datasetconverter;

import at.boisgard.thesis.datasetconverter.builder.BaseBuilder;
import at.boisgard.thesis.datasetconverter.converter.LuisConverter;
import at.boisgard.thesis.datasetconverter.converter.RasaConverter;
import at.boisgard.thesis.datasetconverter.model.luis.Intent;
import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
@EnableAutoConfiguration
@SpringBootApplication
public class ThesisDatasetConverterApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThesisDatasetConverterApplication.class);
    
    @Autowired
    private BaseBuilder baseBuilder;
    
    @PostConstruct
    public void createLUISData(){
        
        LOGGER.info("Converting {} Utterances to LUIS format",baseBuilder.utterancesIncludingSynonyms.size());
        
        LuisConverter lConverter = new LuisConverter(baseBuilder.utterancesIncludingSynonyms);
        
        int nOfFiles = lConverter.convert();
        
        LOGGER.info("Done creating {} LUIS Intents in {} files",lConverter.utterances.size(),nOfFiles);
    }
    
    @PostConstruct
    public void createRasaData(){
        
        LOGGER.info("Converting {} Utterances to rasa format",baseBuilder.utterances.size());
        
        RasaConverter rConverter = new RasaConverter(baseBuilder.utterances);
        
        try {
            
            rConverter.convert();
            LOGGER.info("Done creating {} rasa Intents including {} SynSets",rConverter.utterances.size(), rConverter.synSets.size());
        } catch (IOException e) {
            
            LOGGER.error(e.getMessage());
        }
            
        
    }

    public static void main(String[] args) {
        SpringApplication.run(ThesisDatasetConverterApplication.class, args).close();
        
    }
}
