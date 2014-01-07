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
package org.savantbuild.plugin;

import org.savantbuild.dep.DependencyService.ResolveConfiguration;
import org.savantbuild.dep.DependencyService.ResolveConfiguration.TypeResolveConfiguration;
import org.savantbuild.dep.domain.Artifact;
import org.savantbuild.dep.domain.Dependencies;
import org.savantbuild.dep.domain.Dependency;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.dep.graph.ArtifactGraph;
import org.savantbuild.dep.graph.DependencyGraph;
import org.savantbuild.dep.graph.ResolvedArtifactGraph;
import org.savantbuild.domain.Project;
import org.savantbuild.lang.Classpath;
import org.savantbuild.output.Output;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Default plugin loader that uses the Savant dependency service and a URLClassLoader to load the plugin.
 *
 * @author Brian Pontarelli
 */
public class DefaultPluginLoader implements PluginLoader {
  private final Output output;

  private final Project project;

  public DefaultPluginLoader(Project project, Output output) {
    this.output = output;
    this.project = project;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Plugin load(Dependency pluginDependency) {
    Artifact root = project.toArtifact();
    Dependencies dependencies = new Dependencies(new DependencyGroup("run", false, pluginDependency));
    DependencyGraph dependencyGraph = project.dependencyService.buildGraph(root, dependencies, project.workflow);
    ArtifactGraph artifactGraph = project.dependencyService.reduce(dependencyGraph);
    ResolvedArtifactGraph resolvedArtifactGraph = project.dependencyService.resolve(artifactGraph, project.workflow,
        new ResolveConfiguration().with("run", new TypeResolveConfiguration(true, true)));

    Path pluginJarFilePath = resolvedArtifactGraph.getPath(pluginDependency.id);
    String pluginClassName = null;
    try {
      JarFile pluginJarFile = new JarFile(pluginJarFilePath.toFile());
      Manifest manifest = pluginJarFile.getManifest();
      if (manifest == null) {
        throw new PluginLoadException("Invalid plugin [" + pluginDependency + "]. The JAR file does not contain a valid Manifest entry for Savant-Plugin-Class");
      }

      pluginClassName = manifest.getMainAttributes().getValue("Savant-Plugin-Class");
      if (pluginClassName == null) {
        throw new PluginLoadException("Invalid plugin [" + pluginDependency + "]. The JAR file does not contain a valid Manifest entry for Savant-Plugin-Class");
      }

      Classpath classpath = resolvedArtifactGraph.toClasspath();
      URLClassLoader pluginClassLoader = classpath.toURLClassLoader();
      Class<?> pluginClass = pluginClassLoader.loadClass(pluginClassName);
      return (Plugin) pluginClass.getConstructor(Project.class, Output.class).newInstance(project, output);
    } catch (IOException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin JAR could not be read", e);
    } catch (ClassNotFoundException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] was not in the plugin JAR", e);
    } catch (ClassCastException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] does not extend org.savantbuild.plugin.groovy.Plugin", e);
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new PluginLoadException("Unable to load plugin [" + pluginDependency + "] because the plugin class [" + pluginClassName + "] could not be instantiated", e);
    }
  }
}
