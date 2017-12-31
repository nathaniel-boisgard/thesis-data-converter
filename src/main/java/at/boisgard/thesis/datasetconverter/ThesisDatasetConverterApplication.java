package at.boisgard.thesis.datasetconverter;

import at.boisgard.thesis.datasetconverter.builder.BaseBuilder;
import at.boisgard.thesis.datasetconverter.converter.CoreNLPConverter;
import at.boisgard.thesis.datasetconverter.converter.LuisConverter;
import at.boisgard.thesis.datasetconverter.converter.RasaConverter;
import at.boisgard.thesis.datasetconverter.converter.WatsonConverter;
import at.boisgard.thesis.datasetconverter.converter.WitaiConverter;
import java.io.IOException;
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
    public void createLUISData() {

        LOGGER.info("Converting {} Utterances to LUIS format", baseBuilder.utterances.size());

        LuisConverter lConverter = new LuisConverter(baseBuilder.utterances, baseBuilder.language);

        int nOfFiles = lConverter.convert();

        LOGGER.info("Done creating {} LUIS Intents in {} files", lConverter.utterances.size(), nOfFiles);
    }

    @PostConstruct
    public void createRasaData() {

        LOGGER.info("Converting {} Utterances to rasa format", baseBuilder.utterances.size());

        RasaConverter rConverter = new RasaConverter(baseBuilder.utterances, baseBuilder.language);

        try {

            rConverter.convert();
            LOGGER.info("Done creating {} rasa Intents including {} SynSets", rConverter.utterances.size(), rConverter.synSets.size());
        } catch (IOException e) {

            LOGGER.error(e.getMessage());
        }
    }  
    
    @PostConstruct
    public void createWitAiData() {

        LOGGER.info("Converting {} Utterances to wit.ai format", baseBuilder.utterancesIncludingSynonyms.size());

        WitaiConverter wConverter = new WitaiConverter(baseBuilder.utterancesIncludingSynonyms, baseBuilder.language);

        wConverter.convert();
        LOGGER.info("Done creating {} wit.ai training examples", wConverter.utterances.size());
    }

    @PostConstruct
    public void createCoreNLPData() {

        LOGGER.info("Converting {} Utterances to coreNLP format", baseBuilder.utterancesIncludingSynonyms.size());

        CoreNLPConverter cConverter = new CoreNLPConverter(baseBuilder.utterancesIncludingSynonyms, baseBuilder.language);

        try {

            cConverter.convert();
            LOGGER.info("Done creating {} coreNLP NER training examples", cConverter.utterances.size());
        } catch (IOException e) {

            LOGGER.error(e.getMessage());
        }
    }

    @PostConstruct
    public void createWatsonData() {

        LOGGER.info("Converting {} Utterances to Watson format", baseBuilder.utterances.size());

        WatsonConverter wConverter = new WatsonConverter(baseBuilder.utterances, baseBuilder.language);

        try {

            wConverter.convert();
            LOGGER.info("Done creating {} Watson training examples", wConverter.utterances.size());
        } catch (IOException e) {

            LOGGER.error(e.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ThesisDatasetConverterApplication.class, args).close();

    }
}
