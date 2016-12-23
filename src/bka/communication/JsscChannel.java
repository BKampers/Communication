/*
** Copyright © Bart Kampers
*/

package bka.communication;

import java.util.logging.*;
import jssc.*;


public class JsscChannel extends Channel {


    private JsscChannel(SerialPort serialPort, int baudrate) {
        this.serialPort = serialPort;
        this.baudrate = baudrate;
    }


    private static final StringBuilder builder = new StringBuilder();

    public static void main(String[] args) {
        String name = SerialPortList.getPortNames()[0];
        JsscChannel channel  = JsscChannel.create(name, SerialPort.BAUDRATE_115200);
        try {
            channel.open(null);
            channel.addListener(new ChannelListener() {
                @Override
                public void receive(byte[] bytes) {
                    builder.append(new String(bytes));
//                    System.out.println(new String(bytes));
                }
                @Override
                public void handleException(Exception ex) {
                    ex.printStackTrace(System.err);
                }
            });
            while (builder.length() == 0) {
              channel.send("{a:1}\n".getBytes());
              Thread.sleep(1000);
            }
        }
        catch (ChannelException | InterruptedException ex) {
            Logger.getLogger(JsscChannel.class.getName()).log(Level.SEVERE, "main", ex);
        }
        finally {
            try {
                channel.close();
            }
            catch (ChannelException ex) {
                Logger.getLogger(JsscChannel.class.getName()).log(Level.SEVERE, "close", ex);
            }
        }
        System.out.println(builder.toString());
    }


    public static JsscChannel create(String portName, int baudrate) {
        return new JsscChannel(new SerialPort(portName), baudrate);
    }


    public static JsscChannel create(String portName) {
        return create(portName, SerialPort.BAUDRATE_57600);
    }


    @Override
    public void open(String name) throws ChannelException {
        if (! serialPort.isOpened()) {
            try {
                serialPort.openPort();
                serialPort.setParams(
                    baudrate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
                serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
                serialPort.addEventListener(new EventListener());
            }
            catch (SerialPortException ex) {
                throw new ChannelException(ex);
            }
        }
    }


    @Override
    public void close() throws ChannelException {
        super.close();
        try {
            serialPort.closePort();
        }
        catch (SerialPortException ex) {
            throw new ChannelException(ex);
        }
    }


    @Override
    public void send(byte[] bytes) {
        try {
            serialPort.writeBytes(bytes);
        }
        catch (SerialPortException ex) {
            notifyListeners(ex);
        }
    }


    String readBytes() throws SerialPortException {
        byte[] bytes = serialPort.readBytes();
        if (bytes != null) {
            return new String(bytes);
        }
        else {
            return null;
        }
    }


    private class EventListener implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent evt) {
            if (evt.isRXCHAR() && evt.getEventValue() > 0) {
                try {
                    notifyListeners(serialPort.readBytes());
                }
                catch (SerialPortException ex) {
                    notifyListeners(ex);
                }
            }
        }

    }


    private final SerialPort serialPort;
    private final int baudrate;

}
