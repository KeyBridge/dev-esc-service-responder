/* 
 * Copyright (c) 2021, Key Bridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
   * ServletRequest interface provides HTTP request information.
   */
//  @Context  protected HttpServletRequest httpServletRequest; // requires j2ee (glassfish)
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
    LOG.log(Level.INFO,
            "DpacStatusListenerResource received notice '{'remoteAddr={0}, access_token={1}, messageId={2}, relatesTo={3}, content={4}'}'",
            new Object[]{"unavailable", accessToken, messageID, relatesTo, content});
//            new Object[]{httpServletRequest.getRemoteAddr(), accessToken, messageID, relatesTo, content});

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
