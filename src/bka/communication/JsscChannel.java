/*
** Copyright © Bart Kampers
*/

package bka.communication;

import jssc.*;


public class JsscChannel extends Channel {


    private JsscChannel(SerialPort serialPort, int baudrate) {
        this.serialPort = serialPort;
        this.baudrate = baudrate;
    }


    public static JsscChannel create(String portName, int baudrate) {
        return new JsscChannel(new SerialPort(portName), baudrate);
    }


    public static JsscChannel create(String portName) {
        return create(portName, SerialPort.BAUDRATE_57600);
    }


    public String getName() {
        return serialPort.getPortName();
    }


    public int getBaudrate() {
        return baudrate;
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
    public boolean isOpened() {
        return serialPort.isOpened();
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
