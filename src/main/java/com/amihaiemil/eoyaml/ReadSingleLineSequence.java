/**
 * Copyright (c) 2016-2020, Mihai Emil Andronache
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package com.amihaiemil.eoyaml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Read Yaml Single Line Sequence. This is a Sequence spanning over only one line.
 * This Sequence is comma separated and its children nodes cannot contain above line comments.
 * (Because they all share the same line with the parent Sequence)
 * Example of Single Line Sequence:
 * <pre>
 *   SingleLineSequence: [Text, "More Text"]
 * </pre>
 *
 */
final class ReadSingleLineSequence extends BaseYamlSequence {

    /**
     * All lines of the YAML document.
     */
    private final AllYamlLines all;

    /**
     * Only the significant line of this sequence.
     */
    private final YamlLine significant;

    /**
     * Only the significant portion of the line of this sequence.
     */
    private final String significantPortion;

    /**
     * Whether this is nested in another ReadSingleLineSequence.
     */
    private final boolean nested;

    /**
     * Ctar.
     *
     * @param all All yaml lines.
     * @param significant Only the significant line of this sequence.
     * @param significantPortion Only the significant portion of the line of this sequence.
     * @param nested Whether this is nested in another ReadSingleLineSequence.
     */
    ReadSingleLineSequence(AllYamlLines all, YamlLine significant, String significantPortion, boolean nested) {
        this.all = all;
        this.significant = significant;
        this.significantPortion = significantPortion;
        this.nested = nested;
    }

    @Override
    public Collection<YamlNode> values() {
        final List<YamlNode> kids = new LinkedList<>();
        String trimmed = significantPortion.trim();
        for (String each : trimmed.substring(
                trimmed.indexOf("[") + 1, trimmed.lastIndexOf("]")
        ).split(",[ ]+(?![^\\[]*])")) {
            if (each.startsWith("[") && each.endsWith("]")) {
                kids.add(new ReadSingleLineSequence(all, significant, each, true));
            } else {
                kids.add(new ReadSingleLineSequencePlainLiteralScalar(unescape(each)));
            }
        }
        return kids;
    }

    @Override
    public Comment comment() {
        final Comment comment;
        if (nested) {
            comment = new BuiltComment(this, "");
        } else {
            final int lineNumber = this.significant.number();
            comment = new Concatenated(
                    new ReadComment(
                            new Backwards(
                                    new FirstCommentFound(
                                            new Backwards(
                                                    new Skip(
                                                            this.all,
                                                            line -> line.number() >= lineNumber,
                                                            line -> line.trimmed().startsWith("..."),
                                                            line -> line.trimmed().startsWith("%"),
                                                            line -> line.trimmed().startsWith("!!")
                                                    )
                                            ),
                                            false
                                    )
                            ),
                            this
                    ),
                    new ReadComment(
                            new Skip(
                                    this.all,
                                    line -> line.number() != lineNumber
                            ),
                            this
                    )
            );
        }
        return comment;
    }

    /**
     * Remove the possible escaping quotes or apostrophes surrounding the
     * given value.
     *
     * @param value The value to unescape.
     * @return The value without quotes or apostrophes.
     */
    private String unescape(final String value) {
        final String unescaped;
        if (value == null || value.length() <= 2) {
            unescaped = value;
        } else {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                unescaped = value.substring(1, value.length() - 1);
            } else if (value.startsWith("'") && value.endsWith("'")) {
                unescaped = value.substring(1, value.length() - 1);
            } else {
                unescaped = value;
            }
        }
        return unescaped;
    }

    /**
     * A Plain Literal Scalar for Single Line Sequence
     * This scalar cannot contain comments.
     */
    static class ReadSingleLineSequencePlainLiteralScalar extends BaseScalar {

        /**
         * Value of this scalar.
         */
        private final String value;

        /**
         * Ctor.
         *
         * @param value Given string line.
         */
        ReadSingleLineSequencePlainLiteralScalar(final String value) {
            this.value = value;
        }

        /**
         * Return the value of this literal scalar.
         *
         * @return String value.
         */
        @Override
        public String value() {
            return value;
        }

        @Override
        public Comment comment() {
            return new BuiltComment(this, "");
        }

    }

}
