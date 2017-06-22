package com.akka.logprocessor;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class LogProcessorTest {
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
	   

}
