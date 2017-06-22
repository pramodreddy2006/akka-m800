package com.akka.logprocessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.akka.logprocessor.FileParser.Parser;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class FileScanner extends AbstractActor {

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	final String directoryPath;
	Integer count;

	// FileScanner
	public FileScanner(String directoryPath) {
		this.directoryPath = directoryPath;
		count = 0;
	}

	public static Props props(String directoryPath) {
		return Props.create(FileScanner.class, directoryPath);
	}

	public static final class Scanner {
		public Scanner() {
		}
	}
	
	// #parseFile - Invokes FileParsor actor for the file
	private void parseFile(Path p) {
		String filePath = p.toFile().getAbsolutePath();
		ActorRef parser = getContext().actorOf(FileParser.props(filePath));
		parser.tell(new Parser(), getSelf());
		getContext().watch(parser);
		count++;
	}

	private void parseFiles(Scanner scanner) throws Exception {
		log.debug("In parseFiles for directory {}", directoryPath);
		// Iterating the directory and filtering only files for parsing
		try {
			try (Stream<Path> paths = Files.walk(Paths.get(this.directoryPath))) {
				paths.filter(Files::isRegularFile).forEach(this::parseFile);
			}
		} catch (Exception e) {
			log.error("Error checking files in directory {}" + directoryPath);
		}

		if (getSender() != getContext().system().deadLetters()) {
			getSender().tell(count, getSelf());
		}

		if (count == 0) {
			getContext().stop(getSelf());
		}
	}

	private void onTerminated(Terminated t) {
		count--;
		if (count == 0) {
			getContext().stop(getSelf());
			log.debug("All files in directory parsed");
		}
	}

	@Override
	public Receive createReceive() {
		log.debug("scan request recievied");
		return receiveBuilder().match(Scanner.class, this::parseFiles).match(Terminated.class, this::onTerminated)
				.build();
	}

}
