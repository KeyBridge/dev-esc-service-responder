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

import ch.keybridge.test.rs.ext.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ietf.oauth.message.*;

/**
 * REST client to access the security token service.
 *
 * @author Key Bridge
 * @since v0.1.0 created 2020-12-02
 */
public class StsClient extends AbstractRestClient {

  private static final Logger LOG = Logger.getLogger(StsClient.class.getName());

  /**
   * the default service provider base URI
   */
  public static final String BASE_URI = "https://keybridgewireless.com/sts/api";

  /**
   * The `subject` identifying the customer organization UID identifier. This is
   * required for logging.
   */
  private String subject;

  /**
   * Build a new RestClient instance
   *
   * @param baseURI the base URI
   */
  protected StsClient(String baseURI) {
    super((baseURI != null && !baseURI.trim().isEmpty())
          ? baseURI.trim()
          : BASE_URI);
    LOG.log(Level.INFO, "Sts client with base uri {0}", baseURI);
  }

  /**
   * Get a rest client instance using the provided base URI.
   *
   * @param host (optional) the host URI. if NULL or empty the default will be
   *             used.
   * @return a rest client instance
   */
  public static StsClient getInstance(String host) {
    return new StsClient(BASE_URI.replace("keybridgewireless.com", host));
  }

  /**
   * Get a rest client instance using the default base URI.
   *
   * @return a rest client instance with the default base URI
   */
  public static StsClient getInstance() {
    return new StsClient(BASE_URI);
  }

  /**
   * Set the the customer organization UID identifier. This is required for
   * logging.
   *
   * @param subject The organization UID identifier.
   * @return the current client instance
   */
  public StsClient withSubject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Internal method to generate a standard Jersey HTTP client with a defined
   * socket read timeout.
   * <p>
   * The TCP connect timeout is 10 seconds (down from the default of 60).
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
      /**
       * Disable basic client logging and add the database logging filter below.
       */
      super.setClientLogging(false);
      client = super.buildTrustingClient();
      /**
       * Register the message body reader and writer
       */
      client.register(ClientRegistrationRequestJsonWriter.class);
      client.register(ClientInformationResponseJsonReader.class);
      client.register(ClientUpdateRequestJsonWriter.class);
      client.register(TokenExchangeResponseJsonReader.class);
      client.register(TokenIntrospectionResponseJsonReader.class);
    } catch (Exception exception) {
      LOG.log(Level.WARNING, "StsRestClient build error. May not work if certificates are not valid. {0}", exception.getMessage());
      client = super.buildClient();
    }
    return client.target(baseURI);
  }

  /**
   * Internal method to generate a standard Jersey HTTP client with a defined
   * socket read timeout.
   * <p>
   * The TCP connect timeout is 10 seconds (down from the default of 60).
   *
   * @param clientId the STS client on whose behalf the request is made
   * @return a Jersey HTTP client.
   */
  protected final WebTarget buildWebTarget(String clientId) {
    WebTarget target = buildWebTarget();
    /**
     * If the subject if offered then register the client logging filter.
     */
    if (subject != null) {
//      target.register(new MessageLoggingClientFilter(subject, clientId, "STS"));
//      target.register(new MessageLoggingClientFilter(subject, "Client", "STS"));
    }
    return target;
  }

  public ClientInformationResponse registerClient(ClientRegistrationRequest request, String initialAccessToken) {
    return buildWebTarget(null).path("register")
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + initialAccessToken)
      .post(Entity.json(request))
      .readEntity(ClientInformationResponse.class);
  }

  public ClientInformationResponse readClient(String clientId, String registrationAccessToken) {
    return buildWebTarget(clientId).path("register")
      .path(clientId)
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .get(ClientInformationResponse.class);
  }

  public ClientInformationResponse updateClient(ClientUpdateRequest request, String clientId, String registrationAccessToken) {
    return buildWebTarget(clientId).path("register")
      .path(clientId)
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .put(Entity.json(request))
      .readEntity(ClientInformationResponse.class);
  }

  public Response deleteClient(String clientId, String registrationAccessToken) {
    Response response = buildWebTarget(clientId).path("register")
      .path(clientId)
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken)
      .delete();
    return response;
  }

  public TokenExchangeResponse requestOauthToken(TokenExchangeRequest request, String clientId, String clientSecret) {
    return buildWebTarget(clientId).path("token").path("oauth2")
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, encodeBasicAuthentication(clientId, clientSecret))
      .post(Entity.form(request.toMultivaluedMap()))
      .readEntity(TokenExchangeResponse.class);
  }

  public TokenIntrospectionResponse introspectToken(TokenIntrospectionRequest request, String clientId, String clientSecret) {
    return buildWebTarget(clientId).path("introspect")
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, encodeBasicAuthentication(clientId, clientSecret))
      .post(Entity.form(request.toMultivaluedMap()))
      .readEntity(TokenIntrospectionResponse.class);
  }

  public boolean revokeToken(TokenRevocationRequest request, String clientId, String clientSecret) {
    Response reponse = buildWebTarget(clientId).path("revoke")
      .request(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, encodeBasicAuthentication(clientId, clientSecret))
      .post(Entity.form(request.toMultivaluedMap()));
    return reponse.getStatus() == 200;
  }

  private String encodeBasicAuthentication(String user, String password) {
    String credentialString = user + ":" + password;
    return "Basic " + new String(Base64.getEncoder().encode(credentialString.getBytes()));
  }

}
