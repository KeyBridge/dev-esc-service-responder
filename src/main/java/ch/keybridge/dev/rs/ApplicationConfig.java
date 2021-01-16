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

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Key Bridge
 */
@javax.ws.rs.ApplicationPath("listen")
public class ApplicationConfig extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> resources = new java.util.HashSet<>();
    addRestResourceClasses(resources);
    return resources;
  }

  /**
   * Do not modify addRestResourceClasses() method. It is automatically
   * populated with all resources defined in the project. If required, comment
   * out calling this method in getClasses().
   */
  private void addRestResourceClasses(Set<Class<?>> resources) {
    resources.add(ch.keybridge.dev.rs.DpacStatusListenerResource.class);
    resources.add(ch.keybridge.dev.rs.PingListenerResource.class);
  }

}
