/*
 * Copyright © Bart Kampers
 */

package bka.communication.json;

import bka.communication.*;
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

        @Override
        public boolean isOpened() {
            throw new UnsupportedOperationException("Not required for this test.");
        }

    }


    private static String applicationName;
    

}