/*
 * Copyright © Bart Kampers
 */

package bka.communication.json;

import bka.communication.*;
import org.json.*;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;


@RunWith(MockitoJUnitRunner.class)
public class TransporterTest {


    @BeforeClass
    public static void setUpClass() {
        applicationName = TransporterTest.class.getSimpleName();
    }


    @Test
    public void testName() {
        Channel channelMock = Mockito.mock(TestChannel.class);
        Mockito.when(channelMock.toString()).thenReturn(applicationName);
        Transporter transporter = new Transporter(channelMock, applicationName);
        assertEquals(applicationName, transporter.getName());
    }


    @Test
    public void testOpenClose() throws ChannelException {
        Channel channelMock = Mockito.mock(TestChannel.class);
        Transporter transporter = new Transporter(channelMock, applicationName);
        transporter.open();
        Mockito.verify(channelMock, Mockito.times(1)).open(applicationName);
        transporter.close();
        Mockito.verify(channelMock, Mockito.times(1)).close();
    }


    @Test
    public void testSend() {
        Channel channelMock = Mockito.mock(TestChannel.class);
        Transporter transporter = new Transporter(channelMock, applicationName);
        JSONObject message = new JSONObject();
        byte[] expected = (message.toString() + '\n').getBytes();
        transporter.send(message);
        Mockito.verify(channelMock, Mockito.times(1)).send(expected);
    }


    @Test
    public void testReceive() throws InterruptedException, ChannelException {
        byte[] bytes = "{}\n".getBytes();
        TestChannel testChannel = new TestChannel();
        Transporter transporter = new Transporter(testChannel, applicationName);
        transporter.open();
        testChannel.receive(bytes);
        JSONObject received = transporter.nextReceivedObject();
        assertEquals(0, received.length());
    }


    private class TestChannel extends Channel {

        @Override
        public void open(String name) throws ChannelException {
        }

        @Override
        public void send(byte[] bytes) {
        }

        void receive(byte[] bytes) {
            notifyListeners(bytes);
        }

    }


    private static String applicationName;
    

}