package com.akka.logprocessor;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

import com.akka.logprocessor.Aggregator.*;

public class AggregatorTest {

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
	public void testAggregator() {
		TestKit probe = new TestKit(system);
		ActorRef aggregator = system.actorOf(Aggregator.props("test"));
		aggregator.tell(new Start(), probe.getRef());
		aggregator.tell(new Line("a b c d"), probe.getRef());
		aggregator.tell(new Line("x y z"), probe.getRef());
		aggregator.tell(new End(), probe.getRef());
		@SuppressWarnings("unchecked")
		Optional<Integer> count = probe.expectMsgClass(Optional.class);
		assertEquals("7", count.get().toString());
	}

}
