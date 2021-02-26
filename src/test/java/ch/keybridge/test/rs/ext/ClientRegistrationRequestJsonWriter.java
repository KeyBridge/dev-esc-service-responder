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
import org.ietf.oauth.message.ClientRegistrationRequest;

/**
 *
 * @author Key Bridge
 */
@Produces(MediaType.APPLICATION_JSON)
public class ClientRegistrationRequestJsonWriter implements MessageBodyWriter<ClientRegistrationRequest> {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getSize(ClientRegistrationRequest t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeTo(ClientRegistrationRequest t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
    JsonbBuilder.create().toJson(t, out);
  }

}
