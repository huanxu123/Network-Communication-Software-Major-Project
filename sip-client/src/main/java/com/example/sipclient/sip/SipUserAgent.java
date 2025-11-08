package com.example.sipclient.sip;

import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

import javax.sip.ClientTransaction;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.ViaHeader;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.sip.ObjectInUseException;

/**
 * Simple SIP user agent that can REGISTER and unREGISTER against an MSS registrar.
 * <p>
 * The class follows the minimum set of configuration options needed to let a desktop client
 * authenticate against a Mobicents SIP Server (MSS) instance using the JAIN SIP stack.
 */
public final class SipUserAgent implements SipListener {

    private static final int DEFAULT_EXPIRES_SECONDS = 3600;

    private final String username;
    private final String registrarHost;
    private final int registrarPort;
    private final String password;
    private final String transport;

    private final SipStack sipStack;
    private final SipProvider sipProvider;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final ListeningPoint listeningPoint;
    private final ContactHeader contactHeader;
    private final AuthenticationHelper authenticationHelper;

    private final AtomicLong cseq = new AtomicLong(1);

    private volatile boolean registered;
    private volatile CountDownLatch registrationLatch = new CountDownLatch(0);

    /**
     * Creates a SIP user agent bound to a local socket that can register to MSS.
     *
     * @param userAddress a SIP URI such as {@code sip:alice@example.com}
     * @param password    plaintext password used during digest authentication
     * @param localIp     local IP address that MSS can reach (e.g. {@code 192.168.1.100})
     * @param localPort   local UDP port to bind (e.g. {@code 5070})
     * @throws Exception if the SIP stack cannot be initialised
     */
    public SipUserAgent(String userAddress, String password, String localIp, int localPort) throws Exception {
        Objects.requireNonNull(userAddress, "userAddress");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(localIp, "localIp");

        this.password = password;

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        this.addressFactory = sipFactory.createAddressFactory();
        this.headerFactory = sipFactory.createHeaderFactory();
        this.messageFactory = sipFactory.createMessageFactory();

        SipURI parsedUri = (SipURI) addressFactory.createURI(userAddress);
        if (!"sip".equalsIgnoreCase(parsedUri.getScheme())) {
            throw new IllegalArgumentException("Only sip: URIs are supported");
        }
        this.username = parsedUri.getUser();
        this.registrarHost = parsedUri.getHost();
        this.registrarPort = parsedUri.getPort() == -1 ? 5060 : parsedUri.getPort();
        this.transport = parsedUri.getTransportParam() != null ? parsedUri.getTransportParam() : ListeningPoint.UDP;

        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "SipClientStack");
        properties.setProperty("javax.sip.IP_ADDRESS", localIp);
        // OUTBOUND_PROXY makes sure requests are routed to MSS instead of DNS lookups.
        properties.setProperty("gov.nist.javax.sip.OUTBOUND_PROXY",
                registrarHost + ":" + registrarPort + "/" + transport);
        // Disable verbose logging by default; raise TRACE_LEVEL for troubleshooting.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");

        this.sipStack = sipFactory.createSipStack(properties);
        this.listeningPoint = sipStack.createListeningPoint(localIp, localPort, transport);
        this.sipProvider = sipStack.createSipProvider(listeningPoint);
        this.sipProvider.addSipListener(this);

        this.contactHeader = buildContactHeader(localIp, localPort);

        AccountManager accountManager = () -> new UserCredentials() {
            @Override
            public String getUserName() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public String getSipDomain() {
                return registrarHost;
            }
        };
        this.authenticationHelper = ((SipStackExt) sipStack).getAuthenticationHelper(accountManager, headerFactory);
    }

    /**
     * Performs SIP registration and blocks until MSS responds or the timeout expires.
     *
     * @param timeout how long to wait for the final response
     * @return {@code true} if the 200 OK for REGISTER is received before timeout
     * @throws SipException         when the REGISTER request fails to send
     * @throws InterruptedException if waiting for the response is interrupted
     */
    public boolean register(Duration timeout) throws SipException, InterruptedException {
        return sendRegister(DEFAULT_EXPIRES_SECONDS, timeout);
    }

    /**
     * Unregisters the contact by sending a REGISTER with Expires=0.
     *
     * @param timeout how long to wait for the final response
     * @return {@code true} if MSS confirms the de-registration
     * @throws SipException         when the REGISTER request fails to send
     * @throws InterruptedException if waiting for the response is interrupted
     */
    public boolean unregister(Duration timeout) throws SipException, InterruptedException {
        return sendRegister(0, timeout);
    }

    /**
     * Shuts down the SIP stack and releases sockets.
     */
    public void shutdown() {
        try {
            sipProvider.removeSipListener(this);
        } catch (ObjectInUseException ignored) {
            // Safe to ignore because we are disposing the stack entirely.
        }
        try {
            sipStack.deleteSipProvider(sipProvider);
        } catch (ObjectInUseException ignored) {
        }
        try {
            sipStack.deleteListeningPoint(listeningPoint);
        } catch (ObjectInUseException ignored) {
        }
        sipStack.stop();
    }

    public boolean isRegistered() {
        return registered;
    }

    private boolean sendRegister(int expires, Duration timeout) throws SipException, InterruptedException {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        Request registerRequest = createRegisterRequest(expires);
        ClientTransaction transaction = sipProvider.getNewClientTransaction(registerRequest);
        registrationLatch = new CountDownLatch(1);
        if (expires > 0) {
            registered = false;
        }
        transaction.sendRequest();

        boolean success = registrationLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        return success && registered == (expires > 0);
    }

    private Request createRegisterRequest(int expires) {
        try {
            SipURI requestUri = addressFactory.createSipURI(null, registrarHost);
            requestUri.setPort(registrarPort);
            requestUri.setTransportParam(transport);

            SipURI fromUri = addressFactory.createSipURI(username, registrarHost);
            Address fromAddress = addressFactory.createAddress(fromUri);
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, generateTag());
            ToHeader toHeader = headerFactory.createToHeader(fromAddress, null);

            List<ViaHeader> viaHeaders = Collections.singletonList(
                    headerFactory.createViaHeader(listeningPoint.getIPAddress(),
                            listeningPoint.getPort(), transport, null));

            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq.getAndIncrement(), Request.REGISTER);
            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

            Request request = messageFactory.createRequest(
                    requestUri,
                    Request.REGISTER,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader
            );

            request.addHeader(contactHeader);
            ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(expires);
            request.addHeader(expiresHeader);

            UserAgentHeader userAgentHeader = headerFactory.createUserAgentHeader(
                    List.of("Project-SIP-Client/1.0"));
            request.addHeader(userAgentHeader);

            return request;
        } catch (ParseException | SipException ex) {
            throw new IllegalStateException("Failed to build REGISTER request", ex);
        }
    }

    private ContactHeader buildContactHeader(String localIp, int localPort) throws ParseException {
        SipURI contactUri = addressFactory.createSipURI(username, localIp);
        contactUri.setPort(localPort);
        contactUri.setTransportParam(transport);
        Address contactAddress = addressFactory.createAddress(contactUri);
        ContactHeader header = headerFactory.createContactHeader(contactAddress);
        header.setExpires(DEFAULT_EXPIRES_SECONDS);
        return header;
    }

    private String generateTag() {
        return Long.toHexString(System.currentTimeMillis());
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        // No incoming requests are expected for plain REGISTER flow.
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        int status = response.getStatusCode();

        if (!Request.REGISTER.equals(((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod())) {
            return;
        }

        if (status == Response.UNAUTHORIZED || status == Response.PROXY_AUTHENTICATION_REQUIRED) {
            try {
                ClientTransaction retryTransaction = authenticationHelper.handleChallenge(
                        response,
                        responseEvent.getClientTransaction(),
                        sipProvider,
                        5
                );
                retryTransaction.sendRequest();
                return;
            } catch (Exception ex) {
                registered = false;
                registrationLatch.countDown();
                System.err.println("Failed to respond to authentication challenge: " + ex.getMessage());
                return;
            }
        }

        if (status >= 200 && status < 300) {
            registered = getExpiresFromResponse(response) > 0;
            registrationLatch.countDown();
        } else if (status >= 400) {
            registered = false;
            registrationLatch.countDown();
        }
    }

    private int getExpiresFromResponse(Response response) {
        ExpiresHeader expiresHeader = (ExpiresHeader) response.getHeader(ExpiresHeader.NAME);
        if (expiresHeader != null) {
            return expiresHeader.getExpires();
        }
        ContactHeader responseContact = (ContactHeader) response.getHeader(ContactHeader.NAME);
        if (responseContact != null && responseContact.getExpires() != -1) {
            return responseContact.getExpires();
        }
        return registered ? DEFAULT_EXPIRES_SECONDS : 0;
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        registered = false;
        registrationLatch.countDown();
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        registered = false;
        registrationLatch.countDown();
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        // No-op
    }

    @Override
    public void processDialogTerminated(javax.sip.DialogTerminatedEvent dialogTerminatedEvent) {
        // No-op
    }
}
