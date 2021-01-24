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
package ch.keybridge.dev.rs;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 * <p>
 * Provides a basic DPAC status listener. The response is delayed by a random
 * amount to emulate server processing.
 *
 * @author Key Bridge
 */
@Path("dpac")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DpacStatusListenerResource {

  private static final Logger LOG = Logger.getLogger(DpacStatusListenerResource.class.getName());

  /**
   * A random number generator. Used to delay the response to simulate
   * processing time.
   */
  private static final Random RANDOM = new Random();

  /**
   * Creates a new instance of DpaStatusResource
   */
  public DpacStatusListenerResource() {
  }

  /**
   * HTTP(S) listener end point to receive DpacStatus messages.
   *
   * @see
   * <a href="https://www.w3.org/TR/ws-addr-core/#msgaddrpropsinfoset">Message
   * Addressing Properties</a>
   * @param authorization The HTTP Bearer access token. This is a STS issued
   *                      access_token issued by the ESC to the SAS to
   *                      authenticate the ESC. Use the STS 'introspect' api to
   *                      validate the token against the ESC well known server
   *                      configuration.
   * @param messageID     An absolute IRI that uniquely identifies the message.
   * @param relatesTo     If present, identifies the messageID that this message
   *                      is responding to.
   * @param content       a JSON encoded DpacStatus message object
   * @return http 204 on success, 500 on error
   */
  @PUT
  public Response receiveDpacStatus(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                                    @HeaderParam("MessageID") String messageID,
                                    @HeaderParam("RelatesTo") String relatesTo,
                                    String content) {
    /**
     * Parse the authorization header.
     */
    String accessToken = parseHttpAuthorizationHeader(authorization);
    /**
     * Log the request to console so we know something arrived.
     */
    LOG.log(Level.INFO, "DpacStatusListenerResource '{'access_token={0}, messageId={1}, relatesTo={2}, content={3}'}'", new Object[]{accessToken, messageID, relatesTo, content});
    /**
     * Note that the ESC client is configured to timeout DPAC status message
     * delivery after 2 seconds.
     * <p>
     * Delay the response by up to 2.25 seconds. This will occasionally induce a
     * message processing failure and trigger an error processing sequence on
     * the ESC.
     */
    try {
      Thread.sleep(RANDOM.nextInt(2250)); // simulate processing up to 2.25 seconds
      return Response.noContent().build();  // http 204 on success
    } catch (InterruptedException ex) {
      LOG.log(Level.INFO, "Ping resource interrupted {0}", ex.getMessage());
      return Response.serverError().build(); // http 500 on error
    }
  }

  /**
   * Parse the authorization header to get the bearer credential.
   *
   * @param authorization the authorization header value
   * @return the bearer credential component
   * @throws Exception if no authorization header is present or an invalid
   *                   scheme is offered
   */
  private String parseHttpAuthorizationHeader(String authorization) throws WebApplicationException {
    if (authorization == null || !authorization.matches("^[Bb]earer \\S+$")) {
      LOG.warning("Authorization HTTP header is required with format 'Bearer [credential]'");
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).header("Exception", "Authorization HTTP header is required with format 'Bearer [credential]'").build());
    }
    return authorization.split("\\s")[1].trim();
  }

}
