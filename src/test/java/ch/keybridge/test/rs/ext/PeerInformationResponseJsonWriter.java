/*
 * Copyright 2021 Key Bridge. All rights reserved. Use is subject to license
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
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.opencbrs.peering.message.PeerInformationResponse;

/**
 * JSON-B message body writer.
 *
 * @author Key Bridge
 * @since v0.9.2 created 2021-01-17
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class PeerInformationResponseJsonWriter implements MessageBodyWriter<PeerInformationResponse> {

  /**
   * {@inheritDoc} Always returns `true`.
   */
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return true;
  }

  /**
   * {@inheritDoc} Always returns `0`. Deprecated by JAX-RS 2.0 and ignored by
   * Jersey runtime
   */
  @Override
  public long getSize(PeerInformationResponse t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return 0;
  }

  /**
   * {@inheritDoc} Use JsonbUtility to marshal the object.
   */
  @Override
  public void writeTo(PeerInformationResponse t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    new JsonbUtility().marshal(t, entityStream);
  }

}
