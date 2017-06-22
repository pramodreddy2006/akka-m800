package com.akka.logprocessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.akka.logprocessor.Aggregator.End;
import com.akka.logprocessor.Aggregator.Line;
import com.akka.logprocessor.Aggregator.Start;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class FileParser extends AbstractActor {

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	final String filePath;
	final Path path;
	final ActorRef aggregator;

	public FileParser(String filePath) {
		this.filePath = filePath;
		path = Paths.get(filePath);
		aggregator = getContext().actorOf(Aggregator.props(path.getFileName().toString()));
	}

	public static Props props(String fileName) {
		return Props.create(FileParser.class, fileName);
	}

	public static final class Parser {
		public Parser() {
		}
	}

	public static final class Stop {
		public Stop() {
		}
	}

	private void line(String s) {
		aggregator.forward(new Line(s), getContext());
	}

	// #parse - Parses file invokes aggregator with commands
	private void parse(Parser parser) throws Exception {
		log.debug("In parse for file {}", filePath);
		// watch aggregator actor for this file
		getContext().watch(aggregator);
		try {
			try (Stream<String> stream = Files.lines(path)) {
				aggregator.forward(new Start(), getContext());
				stream.forEach(this::line);
			}
			aggregator.forward(new End(), getContext());
		} catch (Exception e) {
			log.error("Error reading file" + path.getFileName());
			getContext().stop(getSelf());
		}
	}

	// #onTerminated - Stops FileParser actor once aggregator stops.
	private void onTerminated(Terminated t) {
		getContext().stop(getSelf());
	}

	@Override
	public Receive createReceive() {
		log.debug("parse request recieved");
		return receiveBuilder().match(Parser.class, this::parse).match(Terminated.class, this::onTerminated).build();
	}

}
