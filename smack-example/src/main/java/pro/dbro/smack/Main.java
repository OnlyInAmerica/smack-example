package pro.dbro.smack;

import org.jivesoftware.smack.*;

import java.io.IOException;

/**
 * A simple test of the Smack library
 */
public class Main {

    private static final Object mMessageLock = new Object();

    public static void main(String[] args) throws IOException, XMPPException, SmackException, InterruptedException {
        SmackConfiguration.DEBUG_ENABLED = true;
        //testTCPClient();
        testLLClient("adam@dbro.pro");
    }
    /**
     * Listen for XEP-0174 clients, sending each a message
     * as they are discovered. When a response is received,
     * the test ends.
     *
     * @param xmppServiceName
     * @throws XMPPException
     * @throws IOException
     * @throws InterruptedException
     * @throws SmackException
     */
    public static void testLLClient(String xmppServiceName) throws XMPPException, IOException, InterruptedException, SmackException {
        LLXMPPClient llClient = new LLXMPPClient();
        llClient.broadcastPresenceAs(xmppServiceName, false);
        llClient.messageAvailableClients();
    }

//    /**
//     * Starts a standard XMPP Server connection over TCP
//     * and sends messages to a test recipient until a response message
//     * is received, ending the test.
//     *
//     * @throws InterruptedException
//     * @throws XMPPException
//     * @throws SmackException
//     * @throws IOException
//     */
//    public static void testTCPClient() throws InterruptedException, XMPPException, SmackException, IOException {
//        TCPXMPPClient tcpClient = new TCPXMPPClient(SECRETS.XMPP_SERVER, SECRETS.XMPP_PORT, SECRETS.XMPP_SERVICE);
//        tcpClient.loginAs(SECRETS.XMPP_USER, SECRETS.XMPP_PASS);
//        tcpClient.startChatWith(SECRETS.XMPP_RECIPIENT);
//    }

}
