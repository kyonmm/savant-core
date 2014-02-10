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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import groovy.lang.GString;
import static java.util.Arrays.asList;

/**
 * Groovy helpers.
 *
 * @author Brian Pontarelli
 */
public class GroovyTools {
  /**
   * Ensures that the attributes are valid. This checks if the attributes are null and there are no required attributes.
   * If this is the case, it returns true. Otherwise, the attributes must be a Map and must contain the required
   * attributes and have the correct attribute types.
   *
   * @param attributes         The attributes object.
   * @param requiredAttributes A list of required attributes.
   * @param types              The attribute types.
   * @return True if the attributes are valid, false otherwise.
   */
  @SuppressWarnings("unchecked")
  public static boolean attributesValid(Object attributes, Collection<String> requiredAttributes, Map<String, Class<?>> types) {
    if (attributes == null && requiredAttributes.isEmpty()) {
      return true;
    }

    if (attributes == null || !(attributes instanceof Map)) {
      return false;
    }

    Map<String, Object> map = (Map<String, Object>) attributes;
    return hasAttributes(map, requiredAttributes) && hasAttributeTypes(map, types);
  }

  /**
   * Converts all of the list elements to the specified type by calling the function for any list elements that are not
   * the correct type.
   *
   * @param list The list.
   * @param type The type.
   * @param function The function that is used to convert to the correct type.
   * @param <T> The type.
   */
  @SuppressWarnings("unchecked")
  public static <T> void convertListItems(List list, Class<T> type, Function<Object, T> function) {
    for (int i = 0; i < list.size(); i++) {
      if (!type.isInstance(list.get(i))) {
        list.set(i, function.apply(list.get(i)));
      }
    }
  }

  /**
   * Puts all of the values from the defaults map into the main map if they are absent. This is a good way to setup
   * default values.
   *
   * @param map The main map.
   * @param defaults The defaults map.
   */
  public static void putDefaults(Map<String, Object> map, Map<String, Object> defaults) {
    defaults.forEach(map::putIfAbsent);
  }

  /**
   * Checks if the given attributes Map has the correct types. This handles the GString case since that is a Groovy
   * special class that is converted to String dynamically.
   *
   * @param attributes The attributes map.
   * @param types      The types.
   * @return True if the map contains the correct types, false otherwise.
   */
  @SuppressWarnings("unchecked")
  public static boolean hasAttributeTypes(Map<String, Object> attributes, Map<String, Class<?>> types) {
    if (attributes == null) {
      return false;
    }

    for (String key : types.keySet()) {
      Object value = attributes.get(key);
      if (value == null) {
        continue;
      }

      Class type = types.get(key);
      if (type == String.class && !(value instanceof String || value instanceof GString)) {
        return false;
      } else if (type != String.class && !type.isAssignableFrom(value.getClass())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if the given attributes Map has all of the given attribute names. The values for the attribute names must be
   * non-null and non-empty.
   *
   * @param attributes     The attributes map.
   * @param attributeNames The attribute names.
   * @return True if the map contains all of the attribute names, false otherwise.
   */
  public static boolean hasAttributes(Map<String, Object> attributes, Iterable<String> attributeNames) {
    if (attributes == null) {
      return false;
    }

    for (String attributeName : attributeNames) {
      Object value = attributes.get(attributeName);
      if (value == null || (value instanceof CharSequence && value.toString().trim().length() == 0)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if the given attributes Map has all of the given attribute names. The values for the attribute names must be
   * non-null and non-empty.
   *
   * @param attributes     The attributes map.
   * @param attributeNames The attribute names.
   * @return True if the map contains all of the attribute names, false otherwise.
   */
  public static boolean hasAttributes(Map<String, Object> attributes, String... attributeNames) {
    return hasAttributes(attributes, asList(attributeNames));
  }

  /**
   * Converts the object to a List of Strings. If the object is a List, this converts all non-null items to Strings and
   * deletes null values. If it isn't a List, it returns a single element List with the value of object.toString().
   *
   * @param value The value to convert.
   * @return The List of Strings.
   */
  @SuppressWarnings("unchecked")
  public static List<String> toListOfStrings(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof List) {
      List<Object> list = (List<Object>) value;
      return list.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());
    }

    return asList(value.toString());
  }

  /**
   * Safely converts an attribute to a String.
   *
   * @param attributes The attributes.
   * @param key        The key of the attribute to convert.
   * @return Null if the object is null, otherwise the result of calling toString.
   */
  public static String toString(Map<String, Object> attributes, String key) {
    if (attributes == null) {
      return null;
    }

    Object object = attributes.get(key);
    if (object == null) {
      return null;
    }

    return object.toString();
  }
}
