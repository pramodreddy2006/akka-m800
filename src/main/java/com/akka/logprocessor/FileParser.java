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
		aggregator.tell(new Line(s), getSelf());
	}

	//#parse - Parses file invokes aggregator with commands
	private void parse(Parser parser) throws Exception {
		log.debug("In parse for file {}", filePath);
		try {
			try (Stream<String> stream = Files.lines(path)) {
				aggregator.tell(new Start(), getSelf());
				stream.forEach(this::line);
			}
			aggregator.tell(new End(), getSelf());
		} catch (Exception e) {
			log.error("Error reading file" + path.getFileName());
			throw e;
		}
	}
	
	//#stop - Stops FileParser actor once file is processed.
	private void stop(Stop stop) {
		log.debug("Stop FileParser actor for file {}", filePath);
		getContext().stop(getSelf());
	}

	@Override
	public Receive createReceive() {
		log.debug("parse request recieved");
		return receiveBuilder().match(Parser.class, this::parse).match(Stop.class, this::stop).build();
	}

}
