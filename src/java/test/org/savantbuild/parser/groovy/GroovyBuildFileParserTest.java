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

import org.savantbuild.BaseTest;
import org.savantbuild.dep.domain.Dependencies;
import org.savantbuild.dep.domain.Dependency;
import org.savantbuild.dep.domain.DependencyGroup;
import org.savantbuild.dep.domain.Version;
import org.savantbuild.dep.graph.Graph;
import org.savantbuild.dep.graph.HashGraph;
import org.savantbuild.dep.workflow.process.CacheProcess;
import org.savantbuild.dep.workflow.process.URLProcess;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.parser.DefaultTargetGraphBuilder;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the groovy build file parser.
 *
 * @author Brian Pontarelli
 */
public class GroovyBuildFileParserTest extends BaseTest {
  @Test
  public void parse() {
    GroovyBuildFileParser parser = new GroovyBuildFileParser(new DefaultTargetGraphBuilder());
    Project project = parser.parse(projectDir.resolve("src/java/test/org/savantbuild/parser/groovy/simple.savant"));
    assertEquals(project.group, "group");
    assertEquals(project.name, "name");
    assertEquals(project.version, new Version("1.1"));

    // Verify the targets
    assertEquals(project.targets.get("compile").name, "compile");
    assertEquals(project.targets.get("compile").description, "This target compiles everything");
    assertNotNull(project.targets.get("compile").invocation);
    assertNull(project.targets.get("compile").dependencies);

    assertEquals(project.targets.get("test").name, "test");
    assertEquals(project.targets.get("test").description, "This runs the tests");
    assertNotNull(project.targets.get("test").invocation);
    assertEquals(project.targets.get("test").dependencies, asList("compile"));

    // Verify the target graph
    Graph<Target, Object> expected = new HashGraph<>();
    expected.addEdge(project.targets.get("test"), project.targets.get("compile"), Project.GRAPH_EDGE);
    assertEquals(project.targetGraph, expected);

    // Verify the target executes correctly
    project.targets.get("compile").invocation.run();
    assertEquals(project.name, "changed");

    // Verify the workflow
    assertEquals(project.workflow.fetchWorkflow.processes.size(), 2);
    assertTrue(project.workflow.fetchWorkflow.processes.get(0) instanceof CacheProcess);
    assertEquals(((CacheProcess) project.workflow.fetchWorkflow.processes.get(0)).dir, System.getProperty("user.home") + "/.savant/cache");
    assertEquals(((URLProcess) project.workflow.fetchWorkflow.processes.get(1)).url, "http://repository.savantbuild.org");
    assertTrue(project.workflow.fetchWorkflow.processes.get(1) instanceof URLProcess);
    assertEquals(project.workflow.publishWorkflow.processes.size(), 1);
    assertEquals(((CacheProcess) project.workflow.publishWorkflow.processes.get(0)).dir, System.getProperty("user.home") + "/.savant/cache");

    // Verify the dependencies
    Dependencies expectedDependencies = new Dependencies(
        new DependencyGroup("compile", true, new Dependency("org.example:compile:1.0", false)),
        new DependencyGroup("test-compile", false, new Dependency("org.example:test:1.0", false), new Dependency("org.example:test2:2.0", true)));
    assertEquals(project.dependencies, expectedDependencies);
  }
}
