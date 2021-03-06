package prediction.training;

import java.util.List;
import java.util.Set;

import data.events.CategoricalEventType;
import data.stream.FixedStreamWindow;
import episode.pattern.mining.ParallelEpisodePatternMiner;
import episode.pattern.mining.SerialEpisodePatternMiner;
import episode.pattern.storage.EpisodeTrie;
import util.Pair;

public class EpisodeDiscovery {
	
	/***
	 * Returns the episodes that are frequent in the windows. Pair[0] - the frequent serial episodes. Pair[1] - the frequent parallel episodes 
	 * @param windows
	 * @param eventAlphabet
	 * @param s
	 * @return
	 */
	public Pair<EpisodeTrie<List<Boolean>>,EpisodeTrie<List<Boolean>>>  mineFrequentEpisodes(List<FixedStreamWindow> windows, Set<CategoricalEventType> eventAlphabet,double sSerial,double sParallel){
		SerialEpisodePatternMiner serialMiner = new SerialEpisodePatternMiner(windows, eventAlphabet);
		EpisodeTrie<List<Boolean>> serialPatterns = serialMiner.mineFrequentEpisodePatterns(sSerial);
		ParallelEpisodePatternMiner parallelMiner = new ParallelEpisodePatternMiner(windows, eventAlphabet);
		EpisodeTrie<List<Boolean>> parallelPatterns = parallelMiner.mineFrequentEpisodePatterns(sParallel);
		return new Pair<>(serialPatterns,parallelPatterns);
	}	
	
}
