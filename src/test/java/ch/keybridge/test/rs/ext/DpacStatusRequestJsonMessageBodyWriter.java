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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.opencbrs.peering.message.DpacStatusRequest;

/**
 * JSON-B message body writer for the DpacStatusRequest entity class.
 *
 * @author Key Bridge
 * @since v0.12.0 created 2021-02-25
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class DpacStatusRequestJsonMessageBodyWriter implements MessageBodyWriter<DpacStatusRequest> {

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
  public long getSize(DpacStatusRequest t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return 0;
  }

  /**
   * {@inheritDoc} Use JsonbBuilder to marshal the object.
   */
  @Override
  public void writeTo(DpacStatusRequest t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    JsonbBuilder.create().toJson(t, entityStream);
  }

}
