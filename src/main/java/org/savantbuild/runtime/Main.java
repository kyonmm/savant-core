/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.savantbuild.dep.LicenseException;
import org.savantbuild.dep.PublishException;
import org.savantbuild.dep.domain.CompatibilityException;
import org.savantbuild.dep.domain.VersionException;
import org.savantbuild.dep.workflow.ArtifactMetaDataMissingException;
import org.savantbuild.dep.workflow.ArtifactMissingException;
import org.savantbuild.dep.workflow.process.ProcessFailureException;
import org.savantbuild.output.Output;
import org.savantbuild.output.SystemOutOutput;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.savantbuild.parser.ParseException;
import org.savantbuild.parser.groovy.GroovyBuildFileParser;
import org.savantbuild.plugin.PluginLoadException;
import org.savantbuild.security.MD5Exception;
import org.savantbuild.util.CyclicException;

/**
 * Main entry point for Savant CLI runtime.
 *
 * @author Brian Pontarelli
 */
public class Main {
  public static Path projectDir = Paths.get("");

  /**
   * THe main method.
   *
   * @param args CLI arguments.
   */
  public static void main(String... args) {
    RuntimeConfigurationParser runtimeConfigurationParser = new DefaultRuntimeConfigurationParser();
    RuntimeConfiguration runtimeConfiguration = runtimeConfigurationParser.parse(args);
    Output output = new SystemOutOutput(runtimeConfiguration.colorizeOutput);
    if (runtimeConfiguration.debug) {
      output.enableDebug();
    }

    Path buildFile = projectDir.resolve("build.savant");
    if (!Files.isRegularFile(buildFile) || !Files.isReadable(buildFile)) {
      output.error("Build file [build.savant] is missing or not readable.");
      System.exit(1);
    }

    try {
      BuildRunner buildRunner = new DefaultBuildRunner(output, new GroovyBuildFileParser(output, new DefaultTargetGraphBuilder()), new DefaultProjectRunner(output));
      buildRunner.run(buildFile, runtimeConfiguration);
    } catch (ArtifactMetaDataMissingException | ArtifactMissingException | BuildRunException | BuildFailureException |
        CompatibilityException | LicenseException | MD5Exception | ParseException | PluginLoadException |
        ProcessFailureException | PublishException | VersionException e) {
      int lineNumber = determineLineNumber(e);
      output.error(e.getMessage() + (lineNumber > 0 ? " Error occurred on line [" + lineNumber + "]" : ""));
      output.debug(e);
      System.exit(1);
    } catch (CyclicException e) {
      output.error("Your dependencies appear to have cycle. The root message is [" + e.getMessage() + "]");
      output.debug(e);
      System.exit(1);
    } catch (Throwable t) {
      output.error("Build failed due to an exception or error." + (runtimeConfiguration.debug ? "" : " Enable debug using the %s switch to see the stack trace."), RuntimeConfiguration.DEBUG_SWITCH);
      output.debug(t);
      System.exit(1);
    }
  }

  private static int determineLineNumber(Exception e) {
    for (int i = 0; i < e.getStackTrace().length; i++) {
      StackTraceElement ste = e.getStackTrace()[i];
      if (ste.getFileName() != null && ste.getFileName().endsWith(".savant")) {
        return ste.getLineNumber();
      }
    }

    return -1;
  }
}
