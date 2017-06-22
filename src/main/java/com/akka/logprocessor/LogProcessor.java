package com.akka.logprocessor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.akka.logprocessor.FileScanner.*;

public class LogProcessor {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Expects path of directory as argument.");
			System.out.println("Usage : java -jar <jar_file> <directory_path>");
			args = new String[1];
			System.exit(8);
		}
		ActorSystem system = ActorSystem.create("log-processor");
		try {
			String dir = args[0];
			//#create-FileScanner-actor
			final ActorRef fileScanner = 
			        system.actorOf(FileScanner.props(dir), "fileScanner");
			fileScanner.tell(new Scanner(), ActorRef.noSender());
			System.out.println(">>> Press ENTER to exit <<<");
		    System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}

}
