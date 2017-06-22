package com.akka.logprocessor;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.akka.logprocessor.FileParser.Parser;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class FileParserTest {

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
	public void testParserWithEmptyFile() {
		TestKit probe = new TestKit(system);
		ActorRef fileParser = system.actorOf(FileParser.props("test/0.txt"));
		fileParser.tell(new Parser(), probe.getRef());
		@SuppressWarnings("unchecked")
		Optional<Integer> count = probe.expectMsgClass(Optional.class);
		assertEquals("0", count.get().toString());
	}
	
	@Test
	public void testParserWithOneLineFile() {
		TestKit probe = new TestKit(system);
		ActorRef fileParser = system.actorOf(FileParser.props("test/1.txt"));
		fileParser.tell(new Parser(), probe.getRef());
		@SuppressWarnings("unchecked")
		Optional<Integer> count = probe.expectMsgClass(Optional.class);
		assertEquals("1", count.get().toString());
	}
	
	@Test
	public void testParserWithFewLinesFile() {
		TestKit probe = new TestKit(system);
		ActorRef fileParser = system.actorOf(FileParser.props("test/2.txt"));
		fileParser.tell(new Parser(), probe.getRef());
		@SuppressWarnings("unchecked")
		Optional<Integer> count = probe.expectMsgClass(Optional.class);
		assertEquals("33", count.get().toString());
	}
	
	@Test
	public void testParserWithManyLinesFile() {
		TestKit probe = new TestKit(system);
		ActorRef fileParser = system.actorOf(FileParser.props("test/3.txt"));
		fileParser.tell(new Parser(), probe.getRef());
		@SuppressWarnings("unchecked")
		Optional<Integer> count = probe.expectMsgClass(Optional.class);
		assertEquals("1254", count.get().toString());
	}
	
	@Test
	public void testParserWithInvalidFile() {
		TestKit probe = new TestKit(system);
		ActorRef fileParser = system.actorOf(FileParser.props("test/4.txt"));
		fileParser.tell(new Parser(), probe.getRef());
		probe.expectNoMsg();
	}

}
