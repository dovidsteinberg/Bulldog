package org.bulldog.core.gpio;

import junit.framework.TestCase;

import org.bulldog.core.Signal;
import org.bulldog.core.mocks.MockedDigitalOutput;
import org.bulldog.core.util.BulldogUtil;
import org.junit.Before;
import org.junit.Test;

public class TestAbstractDigitalOutput {

	private Pin pin;
	
	@Before
	public void setup() {
		pin = new Pin("Testpin", 0, "A", 0);
		MockedDigitalOutput output = new MockedDigitalOutput(pin);
		pin.addFeature(output);
	}
	
	@Test
	public void testOutput() {
		DigitalOutput output = pin.as(DigitalOutput.class);
		
		TestCase.assertEquals(Signal.Low, output.getAppliedSignal());
		
		output.toggle();
		
		TestCase.assertEquals(Signal.High, output.getAppliedSignal());
		
		output.toggle();
		
		TestCase.assertEquals(Signal.Low, output.getAppliedSignal());
		
		output.applySignal(Signal.Low);
		
		TestCase.assertEquals(Signal.Low, output.getAppliedSignal());
		
		output.applySignal(Signal.High);
		
		TestCase.assertEquals(Signal.High, output.getAppliedSignal());
		
		output.applySignal(Signal.High);
		TestCase.assertEquals(Signal.High, output.getAppliedSignal());
		
		output.applySignal(Signal.Low);
		
		TestCase.assertEquals(Signal.Low, output.getAppliedSignal());
		
		output.applySignal(Signal.High);
		output.toggle();
		
		TestCase.assertEquals(Signal.Low, output.getAppliedSignal());
		
		output.high();
		
		TestCase.assertEquals(Signal.High, output.getAppliedSignal());
		
		output.low();
		
		TestCase.assertEquals(Signal.Low, output.getAppliedSignal());
		
		output.high();
		TestCase.assertTrue(output.isHigh());
		TestCase.assertFalse(output.isLow());
		
		output.toggle();
		TestCase.assertFalse(output.isHigh());
		TestCase.assertTrue(output.isLow());
	}
	
	@Test
	public void testName() {
		DigitalOutput output = pin.as(DigitalOutput.class);
		String name = output.getName();
		TestCase.assertNotNull(name);
	}
	
	
	@Test
	public void testBlinking() {
		DigitalOutput output = pin.as(DigitalOutput.class);
		output.startBlinking(50);
		testBlinking(output, 1000, 50);
		
		output.startBlinking(200);
		testBlinking(output, 1000, 200);
		
		output.stopBlinking();
		TestCase.assertFalse(output.isBlocking());
		
		output.startBlinking(50, 1000);
		TestCase.assertTrue(output.isBlocking());
		BulldogUtil.sleepMs(1050);
		TestCase.assertFalse(output.isBlocking());
	}

	private void testBlinking(DigitalOutput output, long duration, long blinkPeriod) {
		long startTime = System.currentTimeMillis();
		long delta = 0;
		Signal expectedSignal = output.getAppliedSignal();
		while(delta < duration) {
			expectedSignal = expectedSignal == Signal.High ? Signal.Low : Signal.High;
			BulldogUtil.sleepMs((int)blinkPeriod);
			delta = System.currentTimeMillis() - startTime;	
			Signal signal = output.getAppliedSignal();
			expectedSignal = signal;
			TestCase.assertEquals(expectedSignal, signal);
			TestCase.assertTrue(output.isBlocking());
		}
	}
}