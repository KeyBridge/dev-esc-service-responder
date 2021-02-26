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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST client to send PING messages to a peer instance. This client should be
 * called every `n` seconds to ping a peer and affirm the remote peer is alive.
 *
 * @author Key Bridge
 */
public class PeerPingClient extends AbstractRestClient {

  private static final Logger LOG = Logger.getLogger(PeerPingClient.class.getName());
  /**
   * An AtomicLong counter for consistent message identification.
   */
  private static final AtomicLong ATOMIC_LONG = new AtomicLong(System.currentTimeMillis());

  private static final String DEFAULT_URI = "https://keybridgewireless.com/cbrs/esc/gw/api/ping";

  /**
   * Connect timeout interval, in milliseconds. The value MUST be an instance
   * convertible to Integer. A value of zero (0) is equivalent to an interval of
   * infinity. The default value is infinity (0).
   */
  private static final int PING_TIMEOUT_CONNECT = 500;
  /**
   * 500 milliseconds = 1/2 second.
   * <p>
   * Waits if necessary for at most the given time for the computation to
   * complete, and then retrieves its result, if available. Parameters:
   */
  private static final int PING_TIMEOUT_READ = 500;

  /**
   * Construct a new client directed at the default URI.
   *
   * @param baseURI the base URI
   */
  protected PeerPingClient(String baseURI) {
    super(baseURI, PING_TIMEOUT_CONNECT, PING_TIMEOUT_READ);
    super.setClientLogging(true);
  }

  /**
   * Construct a new PeerPingClient instance using the default URI.
   *
   * @return a new ping client instance
   */
  public static PeerPingClient getInstance() {
    return new PeerPingClient(DEFAULT_URI);
  }

  /**
   * Construct a new PeerPingClient instance with a specified URI.
   *
   * @param baseURI the remote host PING URI. This must be a fully qualified
   *                URI.
   * @return a new ping client instance
   */
  public static PeerPingClient getInstance(String baseURI) {
    return new PeerPingClient(baseURI);
  }

  /**
   * Internal method to generate a standard Jersey HTTP client with a defined
   * socket read timeout.
   *
   * @param pingUri the ESC ping listener URI
   * @return a Jersey HTTP client.
   */
  protected final WebTarget buildWebTarget() {
    /**
     * Build a target instance ready to use. Try to use an all-trusting trust
     * manager that ignores all SSL errors. Conditionally register the
     * MessageLoggingClientFilter
     */
    Client client;
    try {
      client = super.buildTrustingClient();
    } catch (Exception exception) {
      LOG.log(Level.WARNING, "PeerPingClient build error. May not work if certificates are not valid. {0}", exception.getMessage());
      client = super.buildClient();
    }
    return client.target(baseURI);
  }

  /**
   * Method to attempt to ping the indicated peer.
   *
   * @param authenticationToken the ESC peer authentication token; this is the
   *                            peer_id
   * @param sensorPing          a sensor ping instance
   * @return true if the peer was successfully pinged
   */
  public boolean pingPeer(String authenticationToken) {
    try {
      /**
       * Get a new message ID.
       */
      long messageId = ATOMIC_LONG.getAndIncrement();
      /**
       * Ping the target
       */
      Future<Response> futureResponse = buildWebTarget().path("ping")
        .request(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationToken)
        .header("MessageId", messageId)
        .async() // throws InterruptedException, ExecutionException
        .get();
      /**
       * Inspect the response header. Check and affirm message addressing.
       */
      Response response = futureResponse.get(TIMEOUT_READ, TimeUnit.SECONDS);
      String relatesTo = response.getHeaderString("RelatesTo");
      if (relatesTo != null && Long.parseLong(relatesTo) != messageId) {
        throw new Exception("Message addressing error: {messageId=" + messageId + ", relatesTo=" + relatesTo + "}");
      }
      /**
       * Expect the response to be HTTP 204 NO_CONTENT.
       */
      return response.getStatus() == Response.Status.OK.getStatusCode();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      LOG.log(Level.INFO, "Ping interrupted '{'error={0}'}'", new Object[]{TIMEOUT_READ + "ms timout exceeded"});
      return false;
    } catch (Exception ex) {
      LOG.log(Level.INFO, "Ping error '{'error={1}'}'", new Object[]{ex.getMessage()});
//      LOG.log(Level.SEVERE, cbrsPeer.toString(), ex);
      return false;
    }
  }
}
