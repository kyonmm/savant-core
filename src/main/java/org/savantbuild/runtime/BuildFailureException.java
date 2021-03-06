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
package org.savantbuild.runtime;

/**
 * Thrown from the build script when the build fails.
 *
 * @author Brian Pontarelli
 */
public class BuildFailureException extends RuntimeException {
  public BuildFailureException() {
    super();
  }

  public BuildFailureException(String message) {
    super(message);
  }

  public BuildFailureException(String message, Throwable cause) {
    super(message, cause);
  }
}
