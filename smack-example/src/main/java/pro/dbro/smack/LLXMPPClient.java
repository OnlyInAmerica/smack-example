package pro.dbro.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.*;

import javax.jmdns.impl.JmDNSImpl;
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
     * Create a test XEP-0174 XMPP client
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
     * @param fullJid            The full JID to use. e.g "david@whatever.com"
     * @param logNetworkActivity Whether to print network events to the console.
     * @throws java.net.UnknownHostException
     * @throws org.jivesoftware.smack.XMPPException
     */
    public void broadcastPresenceAs(final String fullJid, boolean logNetworkActivity) throws XMPPException, UnknownHostException {
        if (mState != STATE.INITIALIZED) throw new IllegalStateException();

        if(!fullJid.contains("@") || !fullJid.split("@")[1].contains(".")) throw new IllegalArgumentException("Invalid jid. Must be of form username@domain.com");

        // Jid of "example@test.com" becomes mDNS Service Name "example@test"
        String serviceName = fullJid.substring(0, fullJid.lastIndexOf("."));
        LLPresence presence = new LLPresence(serviceName);
        presence.setJID(fullJid);
        presence.setServiceName(serviceName);
        if (serviceName.contains("@")) {
            presence.setNick(serviceName.split("@")[0]);
        }

        InetAddress localAddress = InetAddress.getLocalHost();
        mService = (JmDNSService) JmDNSService.create(presence, localAddress);
        LLServiceDiscoveryManager disco = LLServiceDiscoveryManager.getInstanceFor(mService);
        if (logNetworkActivity) {
            attachNetworkActivityLoggers();
        }
        mService.init();
        if (VERBOSE) System.out.println("Broadcasting XEP-0174 presence " + serviceName);

        mState = STATE.PRESENT;
    }

    /**
     * Log all network events. Must be called after {@link #broadcastPresenceAs(String, boolean)}
     */
    private void attachNetworkActivityLoggers() {
        if (mState != STATE.INITIALIZED) throw new IllegalStateException();

        JmDNSService.addLLServiceListener(new LLServiceListener() {
            @Override
            public void serviceCreated(LLService service) {
                System.out.println("JmDNS Service created " + service.getLocalPresence().getServiceName());
            }
        });

        mService.addPresenceListener(new LLPresenceListener() {
            @Override
            public void presenceNew(LLPresence presence) {
                System.out.println("new presence! " + presence.getServiceName());
            }

            @Override
            public void presenceRemove(LLPresence presence) {

            }
        });

        mService.addLLChatListener(new LLChatListener() {
            @Override
            public void newChat(LLChat chat) {
                System.out.println("New chat with " + chat.getServiceName());
            }

            @Override
            public void chatInvalidated(LLChat chat) {
                System.out.println("Chat invalidated with " + chat.getServiceName());
            }
        });

        mService.addServiceStateListener(new LLServiceStateListener() {
            @Override
            public void serviceNameChanged(String newName, String oldName) {
                System.out.println("Service name changed. " + oldName + " changed to " + newName);
            }

            @Override
            public void serviceClosed() {
                System.out.println("Service closed");
            }

            @Override
            public void serviceClosedOnError(Exception e) {
                System.out.println("Service closed with Error");
                e.printStackTrace();
            }

            @Override
            public void unknownOriginMessage(Message e) {
                System.out.println("Unknown origin message " + e.getBody());
            }
        });

        mService.addLLServiceConnectionListener(new LLServiceConnectionListener() {
            @Override
            public void connectionCreated(XMPPLLConnection connection) {
                System.out.println("XMPPLLConnection created to " + connection.getConnectionID());
            }
        });

        mService.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) throws SmackException.NotConnectedException {
                System.out.println("Got packet " + packet.toString());
            }
        }, new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return true;
            }
        });
    }


    /**
     * Attaches a LLPresence Listener to the JmDNS Service and
     * sends Messages to each client as they become available on the network.
     */
    public void messageAvailableClients() {
        mService.addPresenceListener(new LLPresenceListener() {
            @Override
            public void presenceNew(LLPresence presence) {
                if (!presence.getServiceName().equals(mService.getLocalPresence().getServiceName())) {
                    System.out.println("new peer presence! " + presence.getServiceName());
                    // If it isn't our own client presence, make first contact
                    try {
                        LLChat chat = mService.getChat(presence.getServiceName());
                        chat.sendMessage("Hey!");
                        chat.addMessageListener(new LLMessageListener() {

                            @Override
                            public void processMessage(LLChat chat, Message message) {
                                System.out.println("Received response from " + chat.getServiceName() + " " + message.getBody());
                            }
                        });
                    } catch (XMPPException | IOException | SmackException e) {
                        e.printStackTrace();
                        System.out.println("Error sending chat to " + presence.getServiceName());
                    }
                } else {
                    System.out.println("Presence equals local presence service name. Ignoring. jid: " + presence.getJID());
                }
            }

            @Override
            public void presenceRemove(LLPresence presence) {
                System.out.println("removed presence! " + presence);
            }
        });
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

    /**
     * Stop the mDNS Service.
     */
    public void shutdown() {
        try {
            mService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
