package data.stream;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

import data.Change;

public class PredictorPerformance implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int[][] confusionMatrix = new int[3][3];
	
	private int toIndex(Change change){
		if(change==Change.UP){
			return 0;
		} else if(change == Change.EQUAL){
			return 1;
		} else{
			return 2;
		}
	}
	
	public PredictorPerformance(){
		
	}
	
	public PredictorPerformance(Collection<PredictorPerformance> collection ){
		collection.forEach(perf ->this.addAllExamples(perf));
	}
	
	public void addTestExample(Change predicted, Change actual){
		int i = toIndex(actual);
		int j = toIndex(predicted);
		confusionMatrix[i][j]++;
	}

	public double getPrecision(Change change) {
		int i = toIndex(change);
		return (double) confusionMatrix[i][i] / (confusionMatrix[0][i] + confusionMatrix[1][i] + confusionMatrix[2][i] );
	}

	public double getRecall(Change change) {
		int i = toIndex(change);
		return (double) confusionMatrix[i][i] / (confusionMatrix[i][0] + confusionMatrix[i][1] + confusionMatrix[i][2] );
	}
	
	public double getEqualIgnoredRecall(Change change){
		assert(change != Change.EQUAL);
		int i = toIndex(change);
		return (double) confusionMatrix[i][i] / (confusionMatrix[i][0] + confusionMatrix[i][2] );
	}
	
	public double getEqualIgnoredPrecision(Change change){
		assert(change != Change.EQUAL);
		int i = toIndex(change);
		return (double) confusionMatrix[i][i] / (confusionMatrix[0][i] + confusionMatrix[2][i] );
	}
	
	public void printConfusionMatrix(){
		System.out.println("\t\t\t Predicted");
		System.out.println("\t\t UP \t EQUAL \t DOWN");
		System.out.println("\t UP \t " + confusionMatrix[0][0] + " \t " + confusionMatrix[0][1]+ " \t " + confusionMatrix[0][2]);
		System.out.println("Actual \t EQUAL \t " + confusionMatrix[1][0] + " \t " + confusionMatrix[1][1]+ " \t " + confusionMatrix[1][2]);
		System.out.println("\t DOWN \t " + confusionMatrix[2][0] + " \t " + confusionMatrix[2][1]+ " \t " + confusionMatrix[2][2]);
	}

	public void addAllExamples(PredictorPerformance thisDayPerformance) {
		for (int i = 0; i < confusionMatrix.length; i++) {
			for (int j = 0; j < confusionMatrix[0].length; j++) {
				confusionMatrix[i][j] += thisDayPerformance.confusionMatrix[i][j];
			}
		}
	}

	public int getNumClassifiedExamples() {
		int sum = 0;
		for (int i = 0; i < confusionMatrix.length; i++) {
			for (int j = 0; j < confusionMatrix[0].length; j++) {
				sum += confusionMatrix[i][j];
			}
		}
		return sum;
	}

	public double getAccuracy() {
		return ((double) confusionMatrix[0][0] + confusionMatrix[1][1] + confusionMatrix[2][2]) / (double) getNumClassifiedExamples();
	}

	public double getEqualIgnoredAccuracy() {
		return ((double) confusionMatrix[0][0] + confusionMatrix[2][2]) / ((double) confusionMatrix[0][0] + confusionMatrix[2][2] + confusionMatrix[0][2] + confusionMatrix[2][0]);
	}

}