package pro.dbro.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A simple test of the Smack library
 */
public class Main {

    private static final Object mMessageLock = new Object();

    public static void main(String[] args) throws IOException, XMPPException, SmackException, InterruptedException {
        SmackConfiguration.DEBUG_ENABLED = true;
        //testTCPClient();
        testLLClient("you@domain.com");
    }

    public static void testTCPClient() throws InterruptedException, XMPPException, SmackException, IOException {
        TCPXMPPClient tcpClient = new TCPXMPPClient(SECRETS.XMPP_SERVER, SECRETS.XMPP_PORT, SECRETS.XMPP_SERVICE);
        tcpClient.loginAs(SECRETS.XMPP_USER, SECRETS.XMPP_PASS);
        tcpClient.startChatWith(SECRETS.XMPP_RECIPIENT);
    }

    public static void testLLClient(String xmppServiceName) throws XMPPException, IOException, InterruptedException, SmackException {
        LLXMPPClient llClient = new LLXMPPClient();
        llClient.broadcastPresenceAs(xmppServiceName);
        //llClient.startChatWith("somebody@somewhere.com");
    }

}
