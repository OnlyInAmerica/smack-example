package pro.dbro.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.XMPPTCPConnection;

import java.io.IOException;

/**
 * Created by davidbrodsky on 5/7/14.
 */
public class Client {

    public static void main(String [] args) throws IOException, XMPPException, SmackException {
        XMPPConnection connection = new XMPPTCPConnection("jabber.org");
        connection.connect();
        connection.login("mtucker", "password");
        Chat chat = ChatManager.getInstanceFor(connection)
                .createChat("jsmith@jivesoftware.com", new MessageListener() {

                    public void processMessage(Chat chat, Message message) {
                        System.out.println("Received message: " + message);
                    }
                });
        chat.sendMessage("Howdy!");
    }
}
