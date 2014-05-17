package pro.dbro.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;

/**
 * A simple test of the Smack library
 */
public class Client {

    private static final Object mMessageLock = new Object();
    private static boolean mGotMsg = false;

    public static void main(String [] args) throws IOException, XMPPException, SmackException, InterruptedException {
        ConnectionConfiguration config = new ConnectionConfiguration(SECRETS.XMPP_SERVER, SECRETS.XMPP_PORT, SECRETS.XMPP_SERVICE);
        XMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login(SECRETS.XMPP_USER, SECRETS.XMPP_PASS);

        synchronized (mMessageLock) {
            Chat chat = ChatManager.getInstanceFor(connection)
                    .createChat(SECRETS.XMPP_RECIPIENT, new MessageListener() {

                        public void processMessage(Chat chat, Message message) {
                            System.out.println("Received message: " + message);
                            mGotMsg = true;
                            try {
                                chat.sendMessage("Just kidding! See ya later!");
                            } catch (XMPPException | SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                            synchronized (mMessageLock) {
                                mMessageLock.notify();
                            }
                        }
                    });
            while (!mGotMsg) {
                chat.sendMessage("Howdy!");
                System.out.println("Waiting for msg");
                mMessageLock.wait(10 * 1000);
            }
        }
    }
}
