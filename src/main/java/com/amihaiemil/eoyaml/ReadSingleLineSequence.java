package com.amihaiemil.eoyaml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ReadSingleLineSequence extends BaseYamlSequence {

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
