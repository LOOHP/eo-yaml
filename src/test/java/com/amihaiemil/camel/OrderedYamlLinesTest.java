/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.amihaiemil.camel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit tests for {@link OrderedYamlLines}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public final class OrderedYamlLinesTest {

    /**
     * OrderedYamlLines can iterate over the ordered a mapping's
     * lines properly. It should iterate only over the lines which
     * are at the same indentation level.
     */
    @Test
    public void iteratesRightOverMap() {
        final List<YamlLine> lines = new ArrayList<>();
        lines.add(new RtYamlLine("first: ", 0));
        lines.add(new RtYamlLine("  - fourth", 1));
        lines.add(new RtYamlLine("  - fifth", 2));
        lines.add(new RtYamlLine("bay: something", 3));
        lines.add(new RtYamlLine("alba: somethingElse", 4));
        final Iterator<YamlLine> iterator = new OrderedYamlLines(
            new RtYamlLines(lines)
        ).iterator();
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(4));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(3));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(0));
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
    }

    /**
     * OrderedYamlLines can iterate over the ordered a sequence's
     * lines properly. It should iterate only over the lines which
     * are at the same indentation level. Also, dashes lines should
     * be ordered according to their nested value nodes.
     */
    @Test
    public void iteratesRightOverSequence() {
        final List<YamlLine> lines = new ArrayList<>();
        lines.add(new RtYamlLine("- ", 0));
        lines.add(new RtYamlLine("  - alfa", 1));
        lines.add(new RtYamlLine("  - beta", 2));
        lines.add(new RtYamlLine("- ", 3));
        lines.add(new RtYamlLine("  key: value", 4));
        lines.add(new RtYamlLine("- scalar", 5));
        final AbstractYamlLines yaml = new OrderedYamlLines(
            new RtYamlLines(lines)
        );
        final Iterator<YamlLine> iterator = yaml.iterator();
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(5));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(0));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(3));
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
        
    }
    
    /**
     * OrderedYamlLines can indent the ordered lines properly.
     */
    @Test
    public void indentsRight() {
        final List<YamlLine> lines = new ArrayList<>();
        lines.add(new RtYamlLine("fish: ", 0));
        lines.add(new RtYamlLine("  - beta", 1));
        lines.add(new RtYamlLine("  - alfa", 2));
        lines.add(new RtYamlLine("bear: something", 3));
        lines.add(new RtYamlLine("fox: somethingElse", 4));
        final AbstractYamlLines yaml = new OrderedYamlLines(
            new RtYamlLines(lines)
        );
        String expected = "bear: something\n"
            + "fish: \n"
            + "  - alfa\n"
            + "  - beta\n"
            + "fox: somethingElse\n";
        MatcherAssert.assertThat(yaml.indent(0), Matchers.equalTo(expected));
    }
    
    /**
     * OrderedYamlLines can return nested lines (in initial order) for a given
     * line.
     */
    @Test
    public void returnsNestedLinesRight() {
        final List<YamlLine> lines = new ArrayList<>();
        lines.add(new RtYamlLine("first: ", 0));
        lines.add(new RtYamlLine("  - bay", 1));
        lines.add(new RtYamlLine("  - alba", 2));
        lines.add(new RtYamlLine("second: something", 3));
        lines.add(new RtYamlLine("third: somethingElse", 4));
        lines.add(new RtYamlLine("  - sixth", 5));
        AbstractYamlLines yamlLines = new RtYamlLines(lines);
        
        Iterator<YamlLine> iterator = yamlLines.nested(0).iterator();
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(1));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(2));
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
        
        iterator = yamlLines.nested(1).iterator();
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
        
        iterator = yamlLines.nested(3).iterator();
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
        
        iterator = yamlLines.nested(4).iterator();
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(5));
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
    }

    /**
     * Test to check that YamlLine.compareTo(...) works fine.
     */
    @Test
    public void yamlLinesAreOrdered() {
        final List<YamlLine> lines = new ArrayList<>();
        lines.add(new RtYamlLine("Einestien", 0));
        lines.add(new RtYamlLine("  Ben", 1));
        lines.add(new RtYamlLine("Charles", 2));
        lines.add(new RtYamlLine("    Denise", 3));
        lines.add(new RtYamlLine("      Albert", 4));
        lines.add(new RtYamlLine("Sherif", 5));
        Collections.sort(lines);
        
        Iterator<YamlLine> iterator = lines.iterator();
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(4));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(1));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(2));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(3));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(0));
        MatcherAssert.assertThat(iterator.next().number(), Matchers.is(5));
        MatcherAssert.assertThat(iterator.hasNext(), Matchers.is(false));
    }
}
