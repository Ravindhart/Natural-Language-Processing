import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.util.Triple;

import java.util.List;

/**
 *
 * @author Ravindhar
 */
public class StanfordNER {
String serializedClassifier;
AbstractSequenceClassifier<CoreLabel> classifier;
public void Setup() throws Exception
{
    serializedClassifier = "english.all.3class.distsim.crf.ser.gz";
    classifier=CRFClassifier.getClassifier(serializedClassifier);
}
    public boolean isNE(String input) {
        input = classifier.classifyToString(input.toUpperCase());
        input = input.substring(input.lastIndexOf("/") + 1);
        if ("O".equals(input)) {
            return false;
        }
        return true;
    }
}
