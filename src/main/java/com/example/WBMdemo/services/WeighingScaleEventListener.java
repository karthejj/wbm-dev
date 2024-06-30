package com.example.WBMdemo.services;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;

//import org.apache.commons.lang3.;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;//add commons-lang dependency
//import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;//add commons-logging dependency
import org.apache.commons.logging.LogFactory;

/**
 * Serial port event listener.
 * 
 * @author Sergio
 *
 */
public class WeighingScaleEventListener implements SerialPortEventListener {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * port's logical name (e.g.: com1 or com2)
	 */
	private String portName;

	/**
	 * Open port
	 */
	private SerialPort port;

	/**
	 * Port's meta information.
	 */
	private CommPortIdentifier portIdentifier;

	/**
	 * Main constructor..
	 * 
	 * @param port name of the port to connect to
	 * @param retorno
	 */
	public WeighingScaleEventListener(String port) {
		this.portName = port;
	}

	
	/**
	 * Close connection to the port
	 */
	public void close() {
		if (port != null) {
			log.info("Closing serial connection " + portName + "... ");
			port.close();
			port = null;
		}
	}

	/**
	 * open port and wait for data.
	 * 
	 * @throws Exception
	 * @throws PortInUseException
	 */
	public void initializePort() throws Exception {
		try {
			this.portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			Random random = new Random();
			this.port = (SerialPort) portIdentifier.open("WeighingSc", random.nextInt(2000));
			port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			port.addEventListener(this);
			port.notifyOnDataAvailable(true);

		} catch (Exception e) {
			if (port != null) {
				port.close();
			}
			throw e;
		}
	}

	@Override
	public void serialEvent(SerialPortEvent serialPortEvent) {
		InputStream is;
		try {
			is = port.getInputStream();
			int value;
			byte[] result = new byte[0];
			while ((value = is.read()) != -1) {
				result = ArrayUtils.add(result, (byte) value);
			}

			String stringValue = new String(result, "utf-8");
			log.info("Received value (" + stringValue.length() + "): \"" + stringValue + "\"");
		} catch (IOException e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}
	}

	public String getPortName() {
		return portName;
	}
	
	public void setPortName(String portName) {
		this.portName = portName;
	}

	
	public CommPortIdentifier getPortIdentifier() {
		return portIdentifier;
	}

}