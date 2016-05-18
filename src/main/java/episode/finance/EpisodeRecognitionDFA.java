package episode.finance;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class EpisodeRecognitionDFA {

	private int pos;
	private SerialEpisodePattern serialEpisodePattern;
	
	public EpisodeRecognitionDFA(SerialEpisodePattern serialEpisodePattern) {
		pos = 0;
		this.serialEpisodePattern = serialEpisodePattern;
	}
	
	public void reset(){
		pos=0;
	}
	
	public void transition(){
		assert(pos<serialEpisodePattern.length());
		pos++;
	}
	
	public boolean isDone(){
		return pos >= serialEpisodePattern.length();
	}

	public AnnotatedEventType waitsFor(){
		return serialEpisodePattern.get(pos);
	}

	public SerialEpisodePattern getEpiosdePattern() {
		return serialEpisodePattern;
	}
}