import java.util.HashMap;


public class WordCountMap {
	private int totalWords;
	private int docCount;
	private HashMap<String, TermCountWeight> termFreqMap;
	
	public WordCountMap(){
		termFreqMap = new HashMap<String, TermCountWeight>();
	}
	public int getTotalWords() {
		return totalWords;
	}
	public void setTotalWords(int totalWords) {
		this.totalWords = totalWords;
	}
	public int getDocCount() {
		return docCount;
	}
	public void setDocCount(int docCount) {
		this.docCount = docCount;
	}
	public HashMap<String, TermCountWeight> getTermFreqMap() {
		return termFreqMap;
	}
	public void setTermFreqMap(HashMap<String, TermCountWeight> termFreqMap) {
		this.termFreqMap = termFreqMap;
	}
}
