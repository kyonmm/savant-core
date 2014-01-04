/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.parser.groovy;

import org.savantbuild.dep.domain.Dependency;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.parser.ParseException;

import java.util.Map;

/**
 * Groovy delegate that defines the dependencies.
 *
 * @author Brian Pontarelli
 */
public class DependencyDelegate {
  private final DependencyGroup group;

  public DependencyDelegate(DependencyGroup group) {
    this.group = group;
  }

  /**
   * Defines a dependency. This takes a Map of attributes but only the {@code id} attributes is required. This attribute
   * defines the dependency (as a String). The {@code optional} attribute is optional and defines if the dependency is
   * optional.
   *
   * @param attributes The attributes.
   * @return The dependency object.
   * @see Dependency#Dependency(String, boolean)
   */
  public Dependency dependency(Map<String, Object> attributes) {
    if (!GroovyTools.hasAttributes(attributes, "id")) {
      throw new ParseException("Invalid publication definition. It must have the id attribute like this:\n\n" +
          "  dependency(id: \"org.example:foo:0.1.0\", optional: false)");
    }

    String id = GroovyTools.toString(attributes, "id");
    Boolean optional = (Boolean) attributes.get("optional");
    Dependency dependency = new Dependency(id, optional != null ? optional : false);
    group.dependencies.add(dependency);
    return dependency;
  }
}