package bka.communication;


import java.util.*;
import java.io.*;
import javax.comm.*;


public class SerialPortChannel extends Channel implements SerialPortEventListener
{

    public SerialPortChannel(CommPortIdentifier commPortIdentifier) {
        this.commPortIdentifier = commPortIdentifier;
    }
    
    
    public static SerialPortChannel create(String portName) throws NoSuchPortException {
        return new SerialPortChannel(CommPortIdentifier.getPortIdentifier(portName));
    }
        
    
    public void open(String name) throws ChannelException {
        try {
            if (port == null) {
                port = (SerialPort) commPortIdentifier.open(name, 2000);
            }
            port.setSerialPortParams(
                19200, 
                SerialPort.DATABITS_8, 
                SerialPort.STOPBITS_1, 
                SerialPort.PARITY_NONE);
            port.addEventListener(this);
            port.notifyOnDataAvailable(true);
            inputStream = port.getInputStream();
            outputStream = port.getOutputStream();
        }
        catch (Exception ex) {
            throw new ChannelException(ex);
        }
    }
    
    
    public void setBaud(int baud) throws ChannelException {
        try {
            port.setSerialPortParams(
                baud, 
                SerialPort.DATABITS_8, 
                SerialPort.STOPBITS_1, 
                SerialPort.PARITY_NONE);
        }
        catch (Exception ex) {
            throw new ChannelException(ex);
        }
    }
    
    
    public int getBaud() throws ChannelException {
        try {
            return port.getBaudRate();
        }
        catch (NullPointerException ex) {
            throw new ChannelException(ex);
        }
    }
    
    
    public void close() {
        if (port != null) {
            port.removeEventListener();
            port.close();
            port = null;
        }
        inputStream = null;
        outputStream = null;
        super.close();
    }
    
    
    public synchronized void send(byte[] bytes) {
        try {
            outputStream.write(bytes);
        }
	catch (IOException e) {
	    notifyListeners(e);
        }            
    }
    
    
    public void serialEvent(SerialPortEvent ev) {
        try {
            int available = inputStream.available();
    	    byte[] received = new byte[available];
            inputStream.read(received, 0, available);
            notifyListeners(received);
	}
	catch (IOException e) {
            notifyListeners(e);
        }
    }
    
    
    public String toString() {
        return commPortIdentifier.getName();
    }
    
    
    public static Collection<SerialPortChannel> findAll() {
        Collection<SerialPortChannel> all = new ArrayList<SerialPortChannel>();
	Enumeration portList = CommPortIdentifier.getPortIdentifiers();
    	while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getName().startsWith("COM")) {
                all.add(new SerialPortChannel(portId));
            }
        }
        return all;
    }


    private final CommPortIdentifier commPortIdentifier;
    private SerialPort port = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
        
}