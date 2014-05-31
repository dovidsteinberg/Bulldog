package org.bulldog.devices.portexpander;

import java.io.IOException;

import org.bulldog.core.Signal;
import org.bulldog.core.gpio.DigitalInput;
import org.bulldog.core.gpio.Pin;
import org.bulldog.core.gpio.event.InterruptEventArgs;
import org.bulldog.core.gpio.event.InterruptListener;
import org.bulldog.core.io.bus.Bus;
import org.bulldog.core.io.bus.BusConnection;
import org.bulldog.core.io.bus.i2c.I2cBus;
import org.bulldog.core.platform.AbstractPinProvider;

/**
 * This class represents the popular PCF8574(A) I2C Port Expander family.
 * It supports all the features of the PCF8574, including interrupts
 * if you connect the INT signal to a DigitalInput of your board.
 * 
 * You can use all the pins as if they are normal pins or you can read
 * or write the state of all pins directly with the readState/writeState
 * methods.
 * 
 * @author Datenheld
 *
 */
public class PCF8574 extends AbstractPinProvider implements InterruptListener {

	private BusConnection connection;
	private DigitalInput interrupt;
	
	private int state = 0xFF;
	
	public PCF8574(BusConnection connection) throws IOException {
		this(connection, null);
	}

	public PCF8574(I2cBus bus, int address) throws IOException {
		this(bus.createConnection(address), null);
	}
	
	public PCF8574(Bus bus, int address, DigitalInput interrupt) throws IOException {
		this(bus.createConnection(address), interrupt);
	}
	
	public PCF8574(BusConnection connection, DigitalInput interrupt) throws IOException {
		createPins();
		this.connection = connection;
		setInterrupt(interrupt);
	}
	
	private void createPins() {
		for(int i = 0; i <= 7; i++) {
			Pin pin = new Pin("P0", i, "P", i);
			pin.addFeature(new PCF8574DigitalInput(pin, this));
			pin.addFeature(new PCF8574DigitalOutput(pin, this));
		}
	}

	@Override
	public void interruptRequest(InterruptEventArgs args) {
		byte lastKnownState = (byte) state;
		state = readState();
		for(int i = 0; i <= 7; i++) {
			Pin currentPin = getPin(i);
			
			if(!currentPin.isFeatureActive(PCF8574DigitalInput.class)) { continue; }
			
			PCF8574DigitalInput input = currentPin.as(PCF8574DigitalInput.class);
			int lastKnownPinState = (lastKnownState >> currentPin.getAddress()) & 1;
			int currentState = (state >> currentPin.getAddress()) & 1;
			if(lastKnownPinState == currentState) { continue; }
			input.handleInterruptEvent(Signal.fromNumericValue(lastKnownState), Signal.fromNumericValue(currentState));
		}
	}
	
	public byte getLastKnownState() {
		return (byte)state;
	}
	
	public void writeState(byte state) {
		this.state = state;
		try {
			connection.writeByte(state);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte readState() {
		try {
			return connection.readByte();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return (byte)0;
	}
	
	public void setInterrupt(DigitalInput input) {
		if(interrupt != null) {
			interrupt.removeInterruptListener(this);
		}
		
		interrupt = input;
		if(interrupt != null) {
			interrupt.addInterruptListener(this);
		}
	}
}