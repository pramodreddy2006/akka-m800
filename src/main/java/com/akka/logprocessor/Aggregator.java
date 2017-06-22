package com.akka.logprocessor;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.akka.logprocessor.FileParser.*;

public class Aggregator extends AbstractActor {

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	final String fileName;

	public Aggregator(String fileName) {
		this.fileName = fileName;
	}

	public static Props props(String fileName) {
		return Props.create(Aggregator.class, fileName);
	}

	Optional<Integer> wordCount = Optional.empty();

	public static final class Start {
		public Start() {
		}
	}

	public static final class End {
		public End() {
		}
	}

	public static final class Line {
		public final String line;

		public Line(String line) {
			this.line = line;
		}
	}

	//#onStart
	private void onStart(Start start) {
		wordCount = Optional.of(0);
	}

	//#onLine - counts words in a file.
	private void onLine(Line line) throws Exception {
		if(wordCount.isPresent() && line.line != null){
			int lineCount = line.line.split(" ").length;
			wordCount = Optional.of(wordCount.get() + lineCount);
		} else {
		    log.error("Aggregator actor for file {} has encountered exception", fileName);
		    getContext().stop(getSelf());
		    getContext().getParent().tell(new Stop(), ActorRef.noSender());
			throw new Exception("Error counting words");
		}
	}

	//#onEnd - prints words count in a file.
	private void onEnd(End end) {
		if(wordCount.isPresent()){
			System.out.println(fileName + " word count: " + wordCount.get());
		} else {
			log.error("Error countng words in file {}", fileName);
		}
		log.debug("Stop Aggregator actor for file {}", fileName);
		getContext().stop(getSelf());
		getContext().getParent().tell(new Stop(), ActorRef.noSender());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Start.class, this::onStart).match(Line.class, this::onLine)
				.match(End.class, this::onEnd).build();
	}

}
