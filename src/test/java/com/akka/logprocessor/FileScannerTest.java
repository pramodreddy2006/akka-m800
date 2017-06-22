package com.akka.logprocessor;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.akka.logprocessor.FileScanner.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class FileScannerTest {
	
	static ActorSystem system;

	@BeforeClass
	public static void setup() {
		system = ActorSystem.create();
	}

	@AfterClass
	public static void teardown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testScanner() {
		TestKit probe = new TestKit(system);
		ActorRef fileScanner = system.actorOf(FileScanner.props("test"));
		fileScanner.tell(new Scanner(), probe.getRef());
		Integer fileCount = probe.expectMsgClass(Integer.class);
		System.out.println(probe.getLastSender());
		assertEquals("4", fileCount.toString());
	}

}
