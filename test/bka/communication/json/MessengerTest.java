/*
 * Copyright © Bart Kampers
 */

package bka.communication.json;

import bka.communication.*;
import org.json.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.*;


public class MessengerTest {

    public MessengerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void test() throws ChannelException, InterruptedException, JSONException {
        Transporter transporterMock = Mockito.mock(Transporter.class);
        Mockito.when(transporterMock.nextReceivedObject()).thenReturn(new JSONObject());
        MessengerListener listener = new MessengerListener();
        Messenger messenger = new Messenger(transporterMock);
        messenger.setListener(listener);
        messenger.start();
//        messenger.send(new JSONObject("{call:\"test\"}"));
     }

     private class MessengerListener implements Messenger.Listener {

        @Override
        public void notifyMessage(JSONObject message) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void notifyResponse(JSONObject message, JSONObject response) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

     }

}