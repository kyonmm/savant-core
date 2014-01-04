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

import org.savantbuild.dep.domain.Dependencies;
import org.savantbuild.dep.domain.Publication;
import org.savantbuild.dep.workflow.FetchWorkflow;
import org.savantbuild.dep.workflow.PublishWorkflow;
import org.savantbuild.dep.workflow.Workflow;
import org.savantbuild.domain.Project;
import org.savantbuild.output.Output;

import java.util.List;

import groovy.lang.Closure;

/**
 * Groovy delegate that captures the Project configuration from the project build file. The methods on this class
 * capture the configuration from the DSL.
 *
 * @author Brian Pontarelli
 */
public class ProjectDelegate {
  public final Output output;

  public final Project project;

  public ProjectDelegate(Output output, Project project) {
    this.output = output;
    this.project = project;
  }

  /**
   * Configures the project dependencies. This method is called with a closure that contains the dependencies
   * definition. It should look like:
   * <p>
   * <pre>
   *   dependencies {
   *     group(type: "compile") {
   *       dependency("org.example:compile:1.0")
   *     }
   *     group(type: "test-compile") {
   *       dependency("org.example:test:1.0")
   *     }
   *   }
   * </pre>
   *
   * @param closure The closure that is called to setup the Dependencies configuration. This closure uses the delegate
   *                class {@link DependenciesDelegate}.
   * @return The Dependencies.
   */
  public Dependencies dependencies(Closure closure) {
    project.dependencies = new Dependencies();
    closure.setDelegate(new DependenciesDelegate(project.dependencies));
    closure.run();
    return project.dependencies;
  }

  /**
   * Configures the project publications. This method is called with a closure that contains the publication
   * definitions. It should look like:
   * <p>
   * <pre>
   *   publications {
   *     publication(name: "foo", file: "build/jars/foo-${project.version}.jar", source: "build/jars/foo-${project.version}-src.jar")
   *     publication(name: "foo-test", file: "build/jars/foo-test-${project.version}.jar", source: "build/jars/foo-test${project.version}-src.jar")
   *   }
   * </pre>
   *
   * @param closure The closure that is called to setup the publications. This closure uses the delegate class {@link
   *                PublicationDelegate}.
   * @return The list of Publications.
   */
  public List<Publication> publications(Closure closure) {
    closure.setDelegate(new PublicationDelegate(project, project.publications));
    closure.run();
    return project.publications;
  }

  /**
   * Configures the project workflow. This method is called with a closure that contains the workflow definition. It
   * should look like:
   * <p>
   * <pre>
   *   workflow {
   *     fetch {
   *       cache()
   *       url(url: "http://repository.savantbuild.org")
   *     }
   *     publish {
   *       cache()
   *     }
   *   }
   * </pre>
   *
   * @param closure The closure that is called to setup the workflow configuration. This closure uses the delegate class
   *                {@link WorkflowDelegate}.
   * @return The workflow.
   */
  public Workflow workflow(Closure closure) {
    project.workflow = new Workflow(new FetchWorkflow(output), new PublishWorkflow());
    closure.setDelegate(new WorkflowDelegate(output, project.workflow));
    closure.run();
    return project.workflow;
  }
}