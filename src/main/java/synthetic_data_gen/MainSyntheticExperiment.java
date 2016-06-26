package synthetic_data_gen;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.mining.PredictiveMiner;
import reallife_data.finance.yahoo.stock.stream.AnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.InMemoryAnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.StreamMonitor;
import semantic.SemanticKnowledgeCollector;

public class MainSyntheticExperiment {

	public static void main(String[] args) throws IOException {
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
		AnnotatedEventType A = new AnnotatedEventType("randomCompanyNotInTheOtherList",Change.UP);
		System.out.println("Starting data generator");
		Generator gen = new Generator(0.2,4,eventAlphabet,A ,10,100000,20,0.0,300,1,NoiseKind.UNIFORM,true,true,true,true,true,false,StandardDistribution.UNIFORM,StandardDistribution.UNIFORM);
		AnnotatedEventStream stream = gen.getGeneratedStream();
		//print ground truth:
		System.out.println("The following Episodes were embedded:");
		for(int i=0;i<gen.getSourceEpisodes().size();i++){
			System.out.println(gen.getSourceEpisodes().get(i) +"  with weight "+ gen.getWeights().get(i));
		}
		//DIRTY DIRTY COPY:
		int d = 1000;
		PredictiveMiner miner = new PredictiveMiner(stream,A,eventAlphabet,100,15,20,d);
		Map<EpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		Map<EpisodePattern, Integer> inversePredictors = miner.getInitialInversePreditiveEpisodes();
		printTrustScores(predictors);
		printTrustScores(inversePredictors);
		StreamMonitor monitor = new StreamMonitor(predictors,inversePredictors, stream, A, d,new File("resources/logs/performanceLog.txt"));
		System.out.println(monitor.getInvestmentTracker().netWorth());
		monitor.monitor();
		Map<EpisodePattern, Integer> trustScores = monitor.getCurrentTrustScores();
		Map<EpisodePattern, Integer> inverseTrustScores = monitor.getCurrentInverseTrustScores();
		printTrustScores(trustScores);
		System.out.println(monitor.getInvestmentTracker().netWorth());
		System.out.println(monitor.getInvestmentTracker().getPrice());
	}
	
	private static void printTrustScores(Map<EpisodePattern, Integer> trustScores) {
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
	}

}