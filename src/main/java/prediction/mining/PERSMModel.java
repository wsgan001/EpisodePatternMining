package prediction.mining;

import java.util.Map;

import data.Change;
import data.stream.StreamWindow;
import episode.finance.EpisodePattern;

public class PERSMModel implements PredictiveModel {

	private Map<EpisodePattern, Double> predictors;
	private Map<EpisodePattern, Double> inversePredictors;

	public PERSMModel(Map<EpisodePattern, Double> predictors,Map<EpisodePattern, Double> inversePredictors) {
		this.predictors = predictors;
		this.inversePredictors = inversePredictors;
	}

	@Override
	public Change predict(StreamWindow currentWindow) {
		int count = 0;
		count += predictors.keySet().stream().filter(e -> currentWindow.containsPattern(e)).count();
		count -= inversePredictors.keySet().stream().filter(e -> currentWindow.containsPattern(e)).count();
		if(count==0){
			return Change.EQUAL;
		} else if(count>0){
			return Change.UP;
		} else{
			return Change.DOWN;
		}
	}

}