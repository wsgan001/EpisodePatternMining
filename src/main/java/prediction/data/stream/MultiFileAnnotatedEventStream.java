package prediction.data.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import prediction.data.AnnotatedEvent;
import prediction.data.Change;
import prediction.util.StandardDateTimeFormatter;

public class MultiFileAnnotatedEventStream extends AbstractAnnotatedEventStream{

	private List<File> files;
	private int fileIndex = 0;
	private BufferedReader currentReader;
	private List<AnnotatedEvent> currentWindow;
	Predicate<? super AnnotatedEvent> filter;
	private int windowDuration;
	private boolean hasNext = true;
	
	public MultiFileAnnotatedEventStream(List<File> serializedStreams,int windowDuration) throws IOException{
		this(serializedStreams,windowDuration,null);
	}
	
	public MultiFileAnnotatedEventStream(List<File> serializedStreams,int windowDuration,Predicate<? super AnnotatedEvent> filter) throws IOException{
		this.files = serializedStreams;
		this.windowDuration = windowDuration;
		this.filter = filter;
		currentWindow = new LinkedList<>();
		readFromNewFile();
	}

	private void readFromNewFile() throws IOException {
		currentReader = new BufferedReader(new FileReader(files.get(fileIndex)));
		System.out.println("beginning "+files.get(fileIndex).getName());
		currentReader.readLine();
		boolean done = false;
		while(!done){
			String line = currentReader.readLine(); //TODO: case that we have no fitting event in this file?
			AnnotatedEvent event = parseEvent(line);
			if(filter==null || filter.test(event)){
				currentWindow.add(event);
				done = true;
			}
		}
	}

	private AnnotatedEvent parseEvent(String line) {
		String[] tokens = line.split(",");
		LocalDateTime timestamp = LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter());
		AnnotatedEvent event = new AnnotatedEvent(tokens[0].replaceAll("\"", ""), Change.valueOf(tokens[1]), timestamp );
		return event;
	}

	public boolean hasNext() {
		return hasNext;
	}

	public AnnotatedEvent next() throws IOException {
		if(hasNext){
			AnnotatedEvent toReturn = buildCurrent();
			increment();
			return toReturn;
		} else{
			throw new NoSuchElementException();
		}
	}

	private void increment() throws IOException {
		boolean eventAdded = false;
		while(true){
			String line = currentReader.readLine();
			if(line==null || line.equals("")){
				break;
			}
			AnnotatedEvent event = parseEvent(line);
			if(filter==null || filter.test(event)){
				currentWindow.add(event);
				eventAdded = true;
				break;
			}
		}
		if(!eventAdded){
			currentReader.close();
			fileIndex++;
			if(fileIndex==files.size()){
				hasNext = false;
			} else{
				readFromNewFile();
			}
		}
		trimWindow();
	}

	private void trimWindow() {
		while(windowTooLarge()){
			currentWindow.remove(0);
		}
	}

	private boolean windowTooLarge() {
		return ChronoUnit.SECONDS.between(currentWindow.get(0).getTimestamp(), currentWindow.get(currentWindow.size()-1).getTimestamp()) > windowDuration;
	}
	
	/***
	 * Returns a window of the stream in the following: Let t be the timestamp of the event at position pos, where pos is the index of the element that was returned by the last call to next(), the window will then contain all events that have timestamps in the interval [t-d,t),
	 * this means events[pos] is NOT contained in the interval. 
	 * @param d duration (in seconds)
	 * @return
	 */
	public FixedStreamWindow getBackwardsWindow(int d){
		if(d > windowDuration){
			assert(false) : "Requested larger backwards window size than the stream stores";
		}
		LocalDateTime endTimestamp = currentWindow.get(getLastReturnedIndex()).getTimestamp();
		ListIterator<AnnotatedEvent> it = currentWindow.listIterator(getLastReturnedIndex());
		List<AnnotatedEvent> window = new ArrayList<>();
		while(it.hasPrevious()){
			AnnotatedEvent current = it.previous();
			long diffInSeconds = ChronoUnit.SECONDS.between(current.getTimestamp(), endTimestamp);
			if(diffInSeconds >d){
				break;
			} else if(diffInSeconds > 0){ //we need the if, since events may have happened at the same time and occur before in our list
				//add this element to the front of our window
				window.add(0, current);
			}
		}
		return new FixedStreamWindow(window);
	}

	private int getLastReturnedIndex() {
		return currentWindow.size()-2; //-2 because the last element is the one that the user has not yet seen, but has already been parsed from the file
	}

	private AnnotatedEvent buildCurrent() {
		return currentWindow.get(currentWindow.size()-1);
	}

	public StreamWindow getBackwardsWindow() {
		return getBackwardsWindow(windowDuration);
	}

	@Override
	public AnnotatedEvent peek() {
		if(hasNext){
			return buildCurrent();
		} else{
			throw new NoSuchElementException();
		}
	}
}
