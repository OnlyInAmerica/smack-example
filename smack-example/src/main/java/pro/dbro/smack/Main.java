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
        testLLClient("david2@dbro.pro");
    }

    public static void testTCPClient() throws InterruptedException, XMPPException, SmackException, IOException {
        TCPXMPPClient tcpClient = new TCPXMPPClient(SECRETS.XMPP_SERVER, SECRETS.XMPP_PORT, SECRETS.XMPP_SERVICE);
        tcpClient.loginAs(SECRETS.XMPP_USER, SECRETS.XMPP_PASS);
        tcpClient.startChatWith(SECRETS.XMPP_RECIPIENT);
    }

    public static void testLLClient(String xmppServiceName) throws XMPPException, IOException, InterruptedException, SmackException {
        LLXMPPClient llClient = new LLXMPPClient();
        llClient.broadcastPresenceAs(xmppServiceName);
        //llClient.startChatWith("adium@dbro.pro");
    }

//    public static void testLLClient(String xmppServiceName) throws XMPPException, InterruptedException, UnknownHostException {
//        LLPresence presence = new LLPresence(xmppServiceName);
//        presence.setNick(xmppServiceName.split("@")[0]);
//        presence.setJID(xmppServiceName);
//        presence.setServiceName(xmppServiceName);
//
//        InetAddress localAddress = InetAddress.getLocalHost();
//        final JmDNSService jmDNSService = (JmDNSService) JmDNSService.create(presence, localAddress);
//        jmDNSService.addLLServiceConnectionListener(new LLServiceConnectionListener() {
//            @Override
//            public void connectionCreated(XMPPLLConnection connection) {
//                System.out.println("Connection created " + connection.getUser());
//            }
//        });
//        jmDNSService.addPresenceListener(new LLPresenceListener() {
//            @Override
//            public void presenceNew(LLPresence presence) {
//                System.out.println("New presence " + presence.getServiceName());
//                try {
//                    jmDNSService.getChat(presence.getServiceName()).sendMessage("Yo buddy!");
//                    jmDNSService.getChat(presence.getServiceName()).addMessageListener(new LLMessageListener() {
//                        @Override
//                        public void processMessage(LLChat chat, Message message) {
//                            System.out.println("Got message! " + message);
//                            mGotMsg = true;
//                        }
//                    });
//                } catch (XMPPException | SmackException | IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void presenceRemove(LLPresence presence) {
//                System.out.println("Removed presence " + presence.getNode());
//            }
//        });
//        jmDNSService.addServiceStateListener(new LLServiceStateListener() {
//            @Override
//            public void serviceNameChanged(String newName, String oldName) {
//                System.out.println("Service name changed from  " + oldName + " to " + newName);
//            }
//
//            @Override
//            public void serviceClosed() {
//                System.out.println("Service closed");
//            }
//
//            @Override
//            public void serviceClosedOnError(Exception e) {
//                System.out.println("Service closed with error");
//                e.printStackTrace();
//            }
//
//            @Override
//            public void unknownOriginMessage(Message e) {
//                System.out.println("Unknown origin message " + e.getBody());
//            }
//        });
//        jmDNSService.addLLChatListener(new LLChatListener() {
//            @Override
//            public void newChat(LLChat chat) {
//                System.out.println("New chat " + chat.getServiceName());
//                chat.addMessageListener(new LLMessageListener() {
//                    @Override
//                    public void processMessage(LLChat chat, Message message) {
//                        System.out.println("Got message! " + message.getBody());
//                    }
//                });
//            }
//
//            @Override
//            public void chatInvalidated(LLChat chat) {
//
//            }
//        });
//
//        LLServiceDiscoveryManager disco = LLServiceDiscoveryManager.getInstanceFor(jmDNSService);
//
//        jmDNSService.init();
//
//        synchronized (mMessageLock) {
//            while (!mGotMsg) {
//                System.out.println("Waiting for msg");
//                //jmDNSService.spam();
//                mMessageLock.wait(10 * 1000);
//            }
//        }
//        System.out.println("Terminating message wait loop");
//    }
}
