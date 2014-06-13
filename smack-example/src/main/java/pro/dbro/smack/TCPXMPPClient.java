package pro.dbro.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;

/**
 * Created by davidbrodsky on 6/12/14.
 */
public class TCPXMPPClient {
    private static final boolean VERBOSE = true;
    private static final Object mMessageLock = new Object();

    private static enum STATE {

        /* XMPP Server not specified */
        UNINITIALIZED,

        /* XMPP Server specified */
        INITIALIZED,

        /* Connected to XMPP Server */
        CONNECTED,

        /* Authenticated with user credentials */
        AUTHENTICATED
    }

    private STATE mState = STATE.UNINITIALIZED;

    /* Prepared in INITIALIZED state onward */
    private String mXmppServer;
    private int mXmppPort;
    private String mXmppService;
    private boolean mGotMsg;

    /* Prepared in CONNECTED state onward */
    private XMPPTCPConnection mConnection;

    /* Prepared in AUTHENTICATED state onward */
    private String mXmppUsername;
    private String mXmppUserPassword;

    /**
     * Create a test XMPP Client over TCP
     *
     * @param xmppServer  the XMPP server to connect to. e.g "talk.google.com"
     * @param xmppPort    the port on which to connect to xmppServer. e.g 5222
     * @param xmppService the XMPP service name (the to: parameter of the stream stanza). e.g "gmail.com"
     */
    public TCPXMPPClient(String xmppServer, int xmppPort, String xmppService) throws XMPPException, IOException, SmackException {
        init(xmppServer, xmppPort, xmppService);
        connect();
    }

    private void init(String xmppServer, int xmppPort, String xmppService) {
        if (mState != STATE.UNINITIALIZED) throw new IllegalStateException();

        mXmppServer = xmppServer;
        mXmppPort = xmppPort;
        mXmppService = xmppService;
        mGotMsg = false;

        mState = STATE.INITIALIZED;
    }

    private void connect() throws IOException, XMPPException, SmackException {
        if (mState != STATE.INITIALIZED) throw new IllegalStateException();

        ConnectionConfiguration config = new ConnectionConfiguration(mXmppServer, mXmppPort, mXmppService);
        if (VERBOSE) System.out.println("Connecting to " + config.getHostAddresses());
        mConnection = new XMPPTCPConnection(config);
        mConnection.connect();

        mState = STATE.CONNECTED;
    }

    /**
     * Authenticate with this client's XMPP Service.
     *
     * @param xmppUsername     the XMPP user name to authenticate with. Excludes domain. e.g "bob" for bob@gmail.com
     * @param xmppUserPassword the XMPP password to authenticate with.
     */
    public void loginAs(String xmppUsername, String xmppUserPassword) throws IOException, SmackException, XMPPException {
        if (mState != STATE.CONNECTED) throw new IllegalStateException();

        mXmppUsername = xmppUsername;
        mXmppUserPassword = xmppUserPassword;

        if (VERBOSE) System.out.println("Logging in as " + mXmppUsername);
        mConnection.login(mXmppUsername, mXmppUserPassword);

        mState = STATE.AUTHENTICATED;
    }

    /**
     * Start a chat with the specified recipient, blocking until
     * the recipient sends a response message.
     *
     * @param xmppRecipient a recipient to send XMPP chat messages tp. e.g "test@jabber.org"
     * @throws java.io.IOException
     * @throws org.jivesoftware.smack.XMPPException
     * @throws org.jivesoftware.smack.SmackException
     * @throws InterruptedException
     */
    public void startChatWith(String xmppRecipient) throws IOException, XMPPException, SmackException, InterruptedException {
        if (mState != STATE.AUTHENTICATED) throw new IllegalStateException();
        Chat chat = ChatManager.getInstanceFor(mConnection)
                .createChat(xmppRecipient, new MessageListener() {

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
        chat.sendMessage("Howdy!");
        if (VERBOSE) System.out.println("Waiting for msg");
        synchronized (mMessageLock) {
            while (!mGotMsg) {
                mMessageLock.wait(10 * 1000);
            }
        }
        if (VERBOSE) System.out.println("Terminating message wait loop");
    }
}
