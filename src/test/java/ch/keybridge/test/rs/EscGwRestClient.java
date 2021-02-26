/*
 * Copyright 2020 Key Bridge. All rights reserved. Use is subject to license
 * terms.
 *
 * This software code is protected by Copyrights and remains the property of
 * Key Bridge and its suppliers, if any. Key Bridge reserves all rights in and to
 * Copyrights and no license is granted under Copyrights in this Software
 * License Agreement.
 *
 * Key Bridge generally licenses Copyrights for commercialization pursuant to
 * the terms of either a Standard Software Source Code License Agreement or a
 * Standard Product License Agreement. A copy of either Agreement can be
 * obtained upon request by sending an email to info@keybridgewireless.com.
 *
 * All information contained herein is the property of Key Bridge and its
 * suppliers, if any. The intellectual and technical concepts contained herein
 * are proprietary.
 */
package ch.keybridge.test.rs;

import ch.keybridge.test.rs.ext.DpacStatusRequestJsonMessageBodyWriter;
import ch.keybridge.test.rs.ext.PeerInformationResponseJsonReader;
import ch.keybridge.test.rs.ext.PeerRegistrationRequestJsonWriter;
import ch.keybridge.test.rs.ext.PeerUpdateRequestJsonWriter;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opencbrs.peering.message.DpacStatusRequest;
import org.opencbrs.peering.message.PeerInformationResponse;
import org.opencbrs.peering.message.PeerRegistrationRequest;
import org.opencbrs.peering.message.PeerUpdateRequest;

/**
 *
 * @author Key Bridge
 */
public class EscGwRestClient extends AbstractRestClient {

  private static final Logger LOG = Logger.getLogger(EscGwRestClient.class.getName());
  private static final String DEFAULT_URI = "https://localhost:8181/cbrs/esc/gw/api";
  /**
   * 500 milliseconds = 1/2 second.
   * <p>
   * Waits if necessary for at most the given time for the computation to
   * complete, and then retrieves its result, if available. Parameters:
   */
  private static final int RESET_TIMEOUT_READ = 500;

  /**
   * The peer organization UID. This is used to log all messages.
   */
  private String subject;

  /**
   * Construct a new client with the necessary registered filters.
   *
   * @param baseURI the base URI
   */
  protected EscGwRestClient(String baseURI) {
    super(baseURI); // sets the base uri
  }

  public static EscGwRestClient getInstance() {
    return new EscGwRestClient(DEFAULT_URI);
  }

  public static EscGwRestClient getInstance(String baseURI) {
    return new EscGwRestClient(baseURI);
  }

  /**
   * Set the peer organization UID. When set the MessageLoggingClientFilter is
   * registered.
   *
   * @param subject the peer organization UID
   * @return the current client instance
   */
  public EscGwRestClient withSubject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EscGwRestClient withClientLogging(boolean clientLogging) {
    return (EscGwRestClient) super.withClientLogging(clientLogging);
  }

  /**
   * Internal method to generate a standard Jersey HTTP client with a defined
   * socket read timeout.
   *
   * @return a Jersey HTTP client.
   */
  protected final WebTarget buildWebTarget() {
    /**
     * Build a target instance ready to use. Try to use an all-trusting trust
     * manager that ignores all SSL errors.
     */
    Client client;
    try {
      client = super.buildTrustingClient();
      /**
       * Register the message body writers and readers.
       */
      client.register(PeerRegistrationRequestJsonWriter.class);
      client.register(PeerUpdateRequestJsonWriter.class);
      client.register(PeerInformationResponseJsonReader.class);
      client.register(DpacStatusRequestJsonMessageBodyWriter.class);
      /**
       * Conditionally register the MessageLoggingClientFilter
       */
      if (clientLogging && subject != null) {
//        client.register(new MessageLoggingClientFilter(subject, "SAS", "ESC"));
      }
    } catch (Exception exception) {
      LOG.log(Level.WARNING, "PeerPingClient build error. May not work if certificates are not valid. {0}", exception.getMessage());
      client = super.buildClient();
    }
    return client.target(baseURI);
  }

  /**
   * Each SAS shall register with the ESC to receive authorization for ESC
   * services. SAS authorization is implemented through the automated SAS
   * registration process.
   * <p>
   * A SAS Instance registers with the ESC by sending a PeerRegistrationRequest
   * message to the ESC. Upon receiving a PeerRegistrationRequest from the SAS
   * the ESC verifies the included JWT authentication bearer token with the STS
   * and attempts to connect to the SAS by sending a ping and notification
   * message to the indicated SAS listener end points. If message exchange
   * between the ESC and SAS are confirmed the SAS is registered and a peering
   * session is established.
   *
   * @param registrationAccessToken the client registration access token issued
   *                                when the client is registered
   * @param request                 the client update request, containing the
   *                                new client metadata
   * @return the PeerInformationResponse information
   */
  public PeerInformationResponse registerPeerSession(String registrationAccessToken, PeerRegistrationRequest request) {
    return buildWebTarget().path("peer")
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .post(Entity.json(request))
      .readEntity(PeerInformationResponse.class);
  }

  /**
   * Client Update Request To update a previously registered client's
   * registration with an authorization server, the client makes an HTTP PUT
   * request to the client configuration endpoint. This request is authenticated
   * by the registration access token issued to the client.
   *
   * @param registrationAccessToken the client registration access token issued
   *                                when the client is registered
   * @param peerId                  the server assigned peer id value
   * @param request                 the client update request, containing the
   *                                new client metadata
   * @return the updated PeerInformationResponse information
   */
  public PeerInformationResponse updatePeerSession(String registrationAccessToken, String peerId, PeerUpdateRequest request) {
    return buildWebTarget().path("peer").path(peerId)
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .put(Entity.json(request))
      .readEntity(PeerInformationResponse.class);
  }

  /**
   * To read the current configuration of the client on the authorization
   * server, the client makes an HTTP GET request to the client configuration
   * endpoint, authenticating with its registration access token. The following
   * is a non-normative example request:
   *
   * @param registrationAccessToken the client registration access token issued
   *                                when the client is registered
   * @param peerId                  The server assigned peer id. This is the
   *                                CBRS PEER instance ID assigned by the
   *                                registering server (typ. Key Bridge).
   * @return the current PeerInformationResponse information
   */
  public PeerInformationResponse readPeerSession(String registrationAccessToken, String peerId) {
    return buildWebTarget().path("peer").path(peerId)
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .get(PeerInformationResponse.class);
  }

  /**
   * A SAS may re-initialize the peering session to receive a complete update of
   * all DPA – Channels under administration from the ESC by resettings the
   * peering session. A session is reset by submitting a DefaultMessage to the
   * reset end point. To the extent possible the submitted DefaultMessage should
   * indicate a reason for resetting the session.
   * <p>
   * The posted message body is a non-null, empty JSON object such as `{}`. Any
   * message body content is ignored.
   *
   * @param registrationAccessToken the client registration access token issued
   *                                when the client is registered
   * @param peerId                  The peer id. This is the CBRS PEER instance
   *                                ID assigned by the registering server (typ.
   *                                Key Bridge).
   * @return on success an HTTP 204 No Content message
   */
  public boolean resetPeerSession(String registrationAccessToken, String peerId) {
    Future<Response> futureResponse = buildWebTarget().path("peer").path(peerId)
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .async()
      .post(Entity.json("{}")); // empty JSON object
    /**
     * Expect the response to be HTTP 204 NO_CONTENT. The server has
     * successfully fulfilled the request and that there is no additional
     * content to send in the response payload body.
     */
    try {
      return futureResponse.get(RESET_TIMEOUT_READ, TimeUnit.MILLISECONDS).getStatus() == Response.Status.NO_CONTENT.getStatusCode();
    } catch (InterruptedException | ExecutionException | TimeoutException interruptedException) {
      LOG.log(Level.INFO, "Reset peer interrupted '{'peer_id={0}, error={1}'}'", new Object[]{peerId, RESET_TIMEOUT_READ + "ms timout exceeded"});
      return false;
    }
  }

  /**
   * RFC 7592 OAuth 2.0 Dynamic Registration Management 2.3. Client Delete
   * Request
   * <p>
   * A client may deprovision itselt on the authorization server by submitting
   * an HTTP DELETE request to the client configuration endpoint. This request
   * is authenticated by the registration access token issued to the client.
   * <p>
   * The following is a non-normative example request:
   * <pre>
   * DELETE /register/s6BhdRkqt3 HTTP/1.1
   * Host: server.example.com
   * Authorization: Bearer reg-23410913-abewfq.123483</pre>
   *
   * @param registrationAccessToken the client registration access token issued
   *                                when the client is registered
   * @param peerId                  The peer id. This is the CBRS PEER instance
   *                                ID assigned by the registering server (typ.
   *                                Key Bridge).
   * @return on success an HTTP 204 No Content message
   */
  public boolean terminatePeerSession(String registrationAccessToken, String peerId) {
    Response response = buildWebTarget().path("peer").path(peerId)
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .delete();
    /**
     * Expect the response to be HTTP 204 NO_CONTENT. The server has
     * successfully fulfilled the request and that there is no additional
     * content to send in the response payload body.
     */
    return response.getStatus() == Response.Status.NO_CONTENT.getStatusCode();
  }

  /**
   * Request the instant status of an esc type DPA + Channel pair.
   *
   * @param accessToken   A bearer access_token issued by the Security Token
   *                      Service.
   * @param statusRequest A fully populated DpacStatusRequest message
   *                      configuration.
   * @return The instant request returns immediately TRUE if successful.
   */
  public boolean requestDpacStatus(String accessToken, DpacStatusRequest statusRequest) {
    Response response = buildWebTarget().path("dpa").path("status")
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
      .header("MessageID", UUID.randomUUID().toString()) // replace with your own
      .post(Entity.json(statusRequest));
    return response.getStatus() == Response.Status.NO_CONTENT.getStatusCode();
  }

}
