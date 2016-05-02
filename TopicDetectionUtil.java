import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import edu.mit.jwi.*;
import java.net.URL;
import java.util.Arrays;

/* WordNet libraries */
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
/* Stanford Tagger libraries */
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class TopicDetectionUtil {

    HashMap<String, WordCountMap> topicWordMap;
    HashMap<String, Integer> globalCountMap;
    HashMap<String, Integer> topicFrequencyMap;
    Set<String> stopwords;
    Stemmer stm;
    int totalDocsCount;
    static String[] removableStrings = {StringConstants.EQUALTO,
        StringConstants.SINGLE_QUOTE, StringConstants.CLOSE_PARENTHSIS,
        StringConstants.OPEN_PARENTHSIS, StringConstants.FWD_SLASH,
        StringConstants.ASTERIK, StringConstants.PLUS,
        StringConstants.AMPERSAND, StringConstants.DOLLAR,
        StringConstants.COLON, StringConstants.EXCLAMATION,
        StringConstants.HASH, StringConstants.HYPHEN,
        StringConstants.DOUBLE_QUOTE, StringConstants.FLOWER_BRKT_LFT,
        StringConstants.FLOWER_BRKT_RGT, StringConstants.UNDERSCORE,
        StringConstants.PIPE, StringConstants.SQUARE_BRKT_LEFT,
        StringConstants.SQUARE_BRKT_RGT, StringConstants.LESSTHAN,
        StringConstants.GREATTHAN,
        StringConstants.QUESTIONMARK};
     public String baselineSystem(File testFile){
           int i = (int)(10*Math.random());
           Object[] topics = topicWordMap.keySet().toArray();
           return (String)topics[i];
   }

    public TopicDetectionUtil(String mainFolderPath) throws IOException {
        File sourceDirectory = new File(mainFolderPath);
        //StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
        String lemma;
        if (sourceDirectory.exists() && sourceDirectory.isDirectory()) {
            File[] topicDirectories = sourceDirectory.listFiles();
            topicWordMap = new HashMap<String, WordCountMap>();
            topicFrequencyMap = new HashMap<String, Integer>();
            HashMap<String, TermCountWeight> wordCountMap;
            populateStopwords();
            globalCountMap = new HashMap<String, Integer>();
            WordCountMap topicTermsMap;
            int topicFreq = 1;
            stm = new Stemmer();
            for (File topic : topicDirectories) {
                if (topic.isDirectory()) {
                    String topicName = topic.getName();
                    if (topicWordMap.containsKey(topicName)) {
                        topicTermsMap = topicWordMap.get(topicName);
                    } else {
                        topicTermsMap = new WordCountMap();
                    }
                    wordCountMap = topicTermsMap.getTermFreqMap();
                    int topicWordsCount = 0;
                    int docCount = 0;
                    for (File indivFile : topic.listFiles()) {
                        FileReader fileReader = new FileReader(indivFile);
                        while (fileReader.isReady() && !fileReader.isEmpty()) {
                            docCount++;
                            String line = fileReader.readLine();
                            line = formatLine(line);
                            String[] words = line.split(StringConstants.SPACE);
                            for (String word : words) {
                                if (!StringConstants.EMPTY.equals(word)
                                        && word.length() > 2) {
                                    // stemming
                                    for (int j = 0; j < word.toCharArray().length; j++) {
                                        stm.add(word.toCharArray()[j]);
                                    }
                                    stm.stem();
                                    word = stm.toString().toLowerCase();

                                    //word=lemmatizer.lemmatize(word);
                                    topicWordsCount++;
                                    int count = 0;
                                    TermCountWeight termCountObj;
                                    if (wordCountMap.containsKey(word)) {
                                        termCountObj = wordCountMap.get(word);
                                        count = termCountObj.getTermCount();
                                        termCountObj.setTermCount(count + 1);
                                    } else {
                                        termCountObj = new TermCountWeight(1);
                                    }
                                    wordCountMap.put(word, termCountObj);
                                    count = 0;
                                    if (globalCountMap.containsKey(word)) {
                                        count = globalCountMap.get(word);
                                    }
                                    globalCountMap.put(word, count + 1);
                                    topicFrequencyMap.put(word, topicFreq);
                                }
                            }
                        }
                        totalDocsCount++;
                        fileReader.close();
                    }
                    topicTermsMap.setTermFreqMap(wordCountMap);
                    topicTermsMap.setTotalWords(topicWordsCount);
                    topicTermsMap.setDocCount(docCount);
                    topicWordMap.put(topicName, topicTermsMap);
                    topicFreq++;
                }

            }
        } else {
            System.err.println("Error - No Source directory");
        }
    }

    public void populateStopwords() {
        stopwords = new HashSet<String>();
        FileReader fileReader = new FileReader("stopwords.txt");
        while (fileReader.isReady() && !fileReader.isEmpty()) {
            stopwords.add(fileReader.readString());
        }
        fileReader.close();
    }

    public String formatLine(String line) {
        if (line == null || StringConstants.EMPTY.equals(line)) {
            return null;
        }
        for (String removeMe : removableStrings) {
            line = line.replaceAll(removeMe, StringConstants.EMPTY);
        }
//        line = line.replaceAll(StringConstants.COMMA, StringConstants.SPACE);
//        line = line.replaceAll(StringConstants.PERIOD, StringConstants.SPACE);
//        line = line.replaceAll(StringConstants.SEMICOLON, StringConstants.SPACE);
        line = line.replaceAll("\\d", StringConstants.EMPTY);
        line = line.replaceAll("\\s+", StringConstants.SPACE);
        return line;
    }

    public String usingNaiveBayes(File testFile) throws IOException {
        URL url = new URL("file", null, "C:\\Program Files (x86)\\WordNet\\2.1\\dict");
        IDictionary dict = new Dictionary(url);
        Set<String> content_tags = new HashSet<String>(Arrays.asList( "NN", "NNS", "NNP",
                "NNPS",  "VB", "VBD", "VBG", "VBN", "VBP", "VBZ","RB", "RBR", "RBS"));
        //"RB", "RBR", "RBS","JJ", "JJR", "JJS",
        MaxentTagger tagger = new MaxentTagger("english-left3words-distsim.tagger");
        dict.open();
        //StanfordLemmatizer lemmatizer=new StanfordLemmatizer();
        StanfordNER ner=new StanfordNER();
        try{
        ner.Setup();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        float file_count = 0, correct = 0;
        FileReader fileReader;
        String resTopic = null, org_topic;
        String tag, word, tag_string;
        for (File fileEntry : testFile.listFiles()) {
            file_count++;
            fileReader = new FileReader(testFile + "/" + fileEntry.getName());
            org_topic = fileEntry.getName().substring(0, fileEntry.getName().length() - 1);
            HashMap<String, Integer> testFileMap = new HashMap<String, Integer>();
            while (fileReader.isReady() && !fileReader.isEmpty()) {
                String tokenLine = fileReader.readLine();
                tokenLine = formatLine(tokenLine.toLowerCase());
                String[] tokens = tokenLine.split(StringConstants.SPACE);
                for (String token : tokens) {
                    token = tagger.tagString(token);
                    if (token.trim().length() == 0) {
                        continue;
                    }
                    word = token.substring(0, token.lastIndexOf("_"));
                    tag_string = token.substring(token.lastIndexOf("_") + 1, token.length() - 1);

                    if (!stopwords.contains(word)
                            && (!StringConstants.EMPTY.equals(word)) && content_tags.contains(tag_string)) {
                        System.out.println(word+" "+tag_string);
                        int count = 0;
                        if (testFileMap.containsKey(token)) {
                            count = testFileMap.get(token);
                        }
                        testFileMap.put(token, count + 1);
                    }
                }
            }
            String org_word, syn;
            double maxProb = Double.MIN_VALUE;
            IIndexWord idxWord = null;
            Set<String> topicSet = topicWordMap.keySet();
            int totalTopics = topicSet.size();
            int uniqueWords = globalCountMap.size(), synset_size;
            int isNER = 0;
            for (String topic : topicSet) {
                WordCountMap indivWordMap = topicWordMap.get(topic);
                int totalSize = indivWordMap.getTotalWords();
                int docCount = indivWordMap.getDocCount(), flag = 0;
                HashMap<String, TermCountWeight> wordMap = indivWordMap
                        .getTermFreqMap();
                double indivProb = 0.0, tempProb, matches;
                for (String testWord : testFileMap.keySet()) {
                    // Getting the word and pos 
                    org_word = testWord;
                    word = testWord.substring(0, testWord.lastIndexOf("_"));
                    isNER=0;
                    
                        if (ner.isNE(word)) {
                            isNER = 1;
                //            System.out.println(word);
                        }
                    
                    // Stemming
                    for (int j = 0; j < word.toCharArray().length; j++) {
                        stm.add(word.charAt(j));
                    }
                    stm.stem();

                    testWord = stm.toString();

                    //word=lemmatizer.lemmatize(word); 
                    flag = 0;
                    if (wordMap.containsKey(testWord)) {
                        flag = 1;
                        indivProb = indivProb
                                + testFileMap.get(org_word)
                                * (Math.log10(wordMap.get(testWord).getTermCount() + 1) - Math
                                .log10(totalSize + uniqueWords));

                        if(isNER==1)
                            indivProb=indivProb+0;
                    } else // Case where we are checking for the presence of synonyms
                    {
                        tag = org_word.substring(org_word.lastIndexOf("_") + 1, org_word.length() - 1);
                        if (tag.startsWith("J")) {
                            idxWord = dict.getIndexWord(word, POS.ADJECTIVE);
                        } else if (tag.startsWith("N")) {
                            idxWord = dict.getIndexWord(word, POS.NOUN);
                        } else if (tag.startsWith("R")) {
                            idxWord = dict.getIndexWord(word, POS.ADVERB);
                        } else if (tag.startsWith("V")) {
                            idxWord = dict.getIndexWord(word, POS.VERB);
                        }
                        if (idxWord != null) {
                            synset_size = idxWord.getWordIDs().size();
                            Set<String> syn_list = new HashSet<String>();
                            for (int i = 0; i < synset_size; i++) {
                                IWordID wordID = idxWord.getWordIDs().get(i); // 1st meaning
                                IWord iword = dict.getWord(wordID);
                                ISynset synset = iword.getSynset();
                                for (IWord w : synset.getWords()) {
                                    syn = w.getLemma();
                                    for (int j = 0; j < syn.toCharArray().length; j++) {
                                        stm.add(syn.charAt(j));
                                    }
                                    stm.stem();
                                    syn = stm.toString();
                                    syn_list.add(syn);
                                }
                            }
                            matches = 0;
                            tempProb = 0;
                            for (String testword : syn_list) {
                                if (wordMap.containsKey(testword)) {
                                    flag = 1;
                                    matches++;
                                    tempProb = tempProb
                                            + testFileMap.get(org_word)
                                            * (Math.log10(wordMap.get(testword).getTermCount() + 1) - Math
                                            .log10(totalSize + uniqueWords));
                                }
                            }
                            if (flag == 1) {
                                indivProb = indivProb + (tempProb / matches);
                            }
                        }
                    }
                    if (flag == 0) {
                        indivProb = indivProb - testFileMap.get(org_word)
                                * (Math.log10(totalSize + uniqueWords));
                    }
                }

                indivProb = indivProb + (Math.log10(docCount) - Math.log10(totalDocsCount));
                System.out.println(topic + "  " + indivProb);
                if (maxProb == Double.MIN_VALUE || maxProb < indivProb) {
                    maxProb = indivProb;
                    resTopic = topic;
                }
            }
            if (resTopic.contains(org_topic)) {
                correct++;
            }
            System.out.println("                Actual: " + org_topic + " Predicted: " + resTopic);
        }//end of main for loop
        System.out.println("Accuracy: " + (correct / file_count) * 100 + "  " + correct + " " + file_count);
        return resTopic;
    }
    
    public void buildWeights() {
        int N = topicWordMap.size(), df, tf;
        double TfIdf;
        Set<String> topicKeySet = topicWordMap.keySet();
        for (String topic : topicKeySet) {
            HashMap<String, TermCountWeight> termMap = topicWordMap.get(topic)
                    .getTermFreqMap();
            for (String term : termMap.keySet()) {
                TermCountWeight termWeight = termMap.get(term);
                tf = termWeight.getTermCount();
                df = topicFrequencyMap.get(term);
                TfIdf = tf * (Math.log(N) - Math.log(df));
                termWeight.setTermWeight(TfIdf);
                termMap.put(term, termWeight);
            }
        }
    }

    public String usingTfIdfs(File testFile) {
        FileReader fileReader = new FileReader(testFile);
        //HashMap<String, Integer> testFileMap = new HashMap<String, Integer>();
        String topic = null, org_topic;
        Set<String> content_tags = new HashSet<String>(Arrays.asList("JJ", "JJR", "JJS", "NN", "NNS", "NNP",
                "NNPS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"));
        double weight = 0, total_weight = 0, file_count = 0, correct = 0;
        MaxentTagger tagger = new MaxentTagger("english-left3words-distsim.tagger");
        for (File fileEntry : testFile.listFiles()) {
            file_count++;
            weight = 0;
            total_weight = 0;
            fileReader = new FileReader(testFile + "/" + fileEntry.getName());

            org_topic = fileEntry.getName().substring(0, fileEntry.getName().length() - 1);
            HashMap<String, Integer> testFileMap = new HashMap<String, Integer>();
            while (fileReader.isReady() && !fileReader.isEmpty()) {
                String tokenLine = fileReader.readLine();
                tokenLine = formatLine(tokenLine.toLowerCase());
                String[] tokens = tokenLine.split(StringConstants.SPACE);
                for (String token : tokens) {
                    String tag_string = tagger.tagString(token);
                    if (tag_string.trim().length() == 0) {
                        continue;
                    }
                    tag_string = tag_string.substring(tag_string.lastIndexOf("_") + 1, tag_string.length() - 1);

                    if (!stopwords.contains(token)
                            && (!StringConstants.EMPTY.equals(token)) && content_tags.contains(tag_string)) {
                        // stemming
                        for (int j = 0; j < token.toCharArray().length; j++) {
                            stm.add(token.toCharArray()[j]);
                        }
                        stm.stem();

                        token = stm.toString();
                        int count = 0;
                        if (testFileMap.containsKey(token)) {
                            count = testFileMap.get(token);
                        }
                        testFileMap.put(token, count + 1);
                    }
                }
            }
            for (String tmp_topic : topicWordMap.keySet()) {
                total_weight = Double.MIN_VALUE;
                HashMap<String, TermCountWeight> weights = topicWordMap.get(
                        tmp_topic).getTermFreqMap();
                for (String term : testFileMap.keySet()) {
                    if (weights.containsKey(term)) {
                        total_weight = total_weight
                                + weights.get(term).getTermCount()
                                * weights.get(term).getTermWeight();
                    }
                }
                if (weight < total_weight) {
                    weight = total_weight;
                    topic = tmp_topic;
                }

            }
            if (topic.contains(org_topic)) {
                correct++;
            }
            System.out.println("Actual: " + org_topic + " Predicted: " + topic);
        }//end of main for loop
        System.out.println("Accuracy: " + (correct / file_count) * 100);

        return topic;
    }
}
