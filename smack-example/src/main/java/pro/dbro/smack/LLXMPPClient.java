package pro.dbro.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by davidbrodsky on 6/12/14.
 */
public class LLXMPPClient {
    private static final boolean VERBOSE = true;
    private static final Object mMessageLock = new Object();

    private static enum STATE {

        /* XMPP Server not specified */
        UNINITIALIZED,

        /* XMPP Server specified */
        INITIALIZED,

        /* Broadcasting presence */
        PRESENT
    }

    private STATE mState = STATE.UNINITIALIZED;

    /* Prepared in INITIALIZED state onward */
    private String mXmppServer;
    private int mXmppPort;
    private String mXmppService;
    private boolean mGotMsg;

    /* Prepared in PRESENT state onward */
    private JmDNSService mService;

    /**
     * Create a test XMPP Client over TCP
     */
    public LLXMPPClient() {
        init();
    }

    private void init() {
        if (mState != STATE.UNINITIALIZED) ;

        mGotMsg = false;

        mState = STATE.INITIALIZED;
    }

    /**
     * Broadcast to XEP-0174 clients with the given service name
     *
     * @param serviceName The name of the XEP-0174 service to broadcast as. e.g "david@whatever.com"
     * @throws IOException
     * @throws XMPPException
     * @throws SmackException
     */
    public void broadcastPresenceAs(String serviceName) throws XMPPException, UnknownHostException {
        if (mState != STATE.INITIALIZED) throw new IllegalStateException();

        LLPresence presence = new LLPresence(serviceName);
        presence.setJID(serviceName);
        presence.setServiceName(serviceName);
        presence.setJID(serviceName);
        if (serviceName.contains("@")) {
            presence.setNick(serviceName.split("@")[0]);
        }

        InetAddress localAddress = InetAddress.getLocalHost();
        mService = (JmDNSService) JmDNSService.create(presence, localAddress);
        LLServiceDiscoveryManager disco = LLServiceDiscoveryManager.getInstanceFor(mService);
        mService.init();
        mService.addPresenceListener(new LLPresenceListener() {
            @Override
            public void presenceNew(LLPresence presence) {
                System.out.println("new presence! " + presence.getServiceName());
                try {
                    mService.getChat(presence.getServiceName()).sendMessage("Hey man!");
                } catch (XMPPException | IOException | SmackException e) {
                    e.printStackTrace();
                    System.out.println("Error sending chat!");
                }
            }

            @Override
            public void presenceRemove(LLPresence presence) {
                System.out.println("removed presence! " + presence);
            }
        });

        mState = STATE.PRESENT;
    }

    /**
     * Start a chat with the specified recipient, blocking until
     * the recipient sends a response message.
     *
     * @param serviceRecipient a XEP-0174 recipient to send XMPP chat messages to. e.g "test@jabber.org"
     * @throws java.io.IOException
     * @throws org.jivesoftware.smack.XMPPException
     * @throws org.jivesoftware.smack.SmackException
     * @throws InterruptedException
     */
    public void startChatWith(String serviceRecipient) throws XMPPException, IOException, SmackException, InterruptedException {

        if (mState != STATE.PRESENT) throw new IllegalStateException();

        mService.getPresenceByServiceName(serviceRecipient);

        mService.getChat(serviceRecipient).addMessageListener(new LLMessageListener() {

            @Override
            public void processMessage(LLChat chat, Message message) {
                mGotMsg = true;
                System.out.println("Got message! " + message.getBody());
            }
        });
        mService.getChat(serviceRecipient).sendMessage("Ahoy!");

        if (VERBOSE) System.out.println("Waiting for msg");
        synchronized (mMessageLock) {
            while (!mGotMsg) {
                mMessageLock.wait(10 * 1000);
            }
        }
        if (VERBOSE) System.out.println("Terminating message wait loop");
        mService.close();

        mState = STATE.UNINITIALIZED;
    }
}
