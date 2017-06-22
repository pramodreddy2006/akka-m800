package com.akka.logprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.akka.logprocessor.FileParser.*;

public class FileScanner extends AbstractActor {

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	final String directoryPath;
	
	//FileScanner
	public FileScanner(String directoryPath) {
		this.directoryPath = directoryPath;
	}
	
	public static Props props(String directoryPath) {
		return Props.create(FileScanner.class, directoryPath);
	}
	
	public static final class Scanner {
		public Scanner() {
		}
	}

	//#parseFile - Invokes FileParsor actor for the file
	private void parseFile(Path p) {
		String filePath = p.toFile().getAbsolutePath();
		ActorRef parser = getContext().actorOf(FileParser.props(filePath));
		parser.tell(new Parser(), getSelf());
	}

	private void parseFiles(Scanner scanner) throws IOException {
		log.debug("In parseFiles for directory {}", directoryPath);
		//Iterating the directory and filtering only files for parsing
		try (Stream<Path> paths = Files.walk(Paths.get(this.directoryPath))) {
			paths.filter(Files::isRegularFile).forEach(this::parseFile);
		}
	}

	@Override
	public Receive createReceive() {
		log.debug("scan request recievied");
		return receiveBuilder().match(Scanner.class, this::parseFiles).build();
	}

}
