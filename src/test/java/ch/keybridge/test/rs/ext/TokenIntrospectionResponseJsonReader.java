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
package ch.keybridge.test.rs.ext;

import ch.keybridge.json.JsonbUtility;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import org.ietf.oauth.message.TokenIntrospectionResponse;

/**
 * JSON message body reader instance.
 *
 * @author Key Bridge
 * @since v0.0.1 created 2020-10-14
 */
@Consumes(MediaType.APPLICATION_JSON)
public class TokenIntrospectionResponseJsonReader implements MessageBodyReader<TokenIntrospectionResponse> {

  @Override
  public boolean isReadable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
    return true;
  }

  @Override
  public TokenIntrospectionResponse readFrom(Class<TokenIntrospectionResponse> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, String> mm, InputStream in) throws IOException, WebApplicationException {
    /**
     * The response can be either a ClientInformationResponse OR an
     * ErrorResponse message object. Determine which by reading the response
     * into a string and looking for an 'error' element.
     */
    String result = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
    /**
     * Parse the response string in to a generic JsonObject. If the JsonObject
     * contains an 'error' element then it is a ErrorResponse.
     */
    JsonObject object = Json.createReader(new StringReader(result)).readObject();
    if (object.containsKey("error")) {
      throw new WebApplicationException(object.getString("error_description"), Response.Status.BAD_REQUEST);
    }
    /**
     * The response is a valid ClientInformationResponse.
     */
    return new JsonbUtility().unmarshal(result, TokenIntrospectionResponse.class);
  }

}
