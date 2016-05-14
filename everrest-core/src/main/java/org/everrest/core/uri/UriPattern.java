/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.uri;

import org.everrest.core.util.UriPatternComparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriPattern {
    // TODO: remove static, create comparator when need it
    public static final Comparator<UriPattern> URIPATTERN_COMPARATOR = new UriPatternComparator();

    /** Should be added in URI pattern regular expression. */
    private static final String URI_PATTERN_TAIL = "(/.*)?";

    //

    /** List of names for URI template variables. */
    private final List<String> parameterNames;

    /** URI template. */
    private final String template;

    /** Number of characters in URI template NOT resulting from template variable substitution. */
    private final int numberOfCharacters;

    /** Compiled URI pattern. */
    private final Pattern pattern;

    /** Regular expressions for URI pattern. */
    private final String regex;

    /** Regex capturing group indexes. */
    private final int[] groupIndexes;

    //

    public UriPattern(String template) {
        if (template.length() > 0 && template.charAt(0) != '/') {
            template = '/' + template;
        }

        UriTemplateParser parser = new UriTemplateParser(template);
        this.template = parser.getTemplate();
        this.parameterNames = Collections.unmodifiableList(parser.getParameterNames());
        this.numberOfCharacters = parser.getNumberOfLiteralCharacters();

        int[] indexes = parser.getGroupIndexes();
        if (indexes != null) {
            this.groupIndexes = new int[indexes.length + 1];
            System.arraycopy(indexes, 0, this.groupIndexes, 0, indexes.length);
            // Add one more index for URI_PATTERN_TAIL
            this.groupIndexes[groupIndexes.length - 1] = indexes[indexes.length - 1] + 1;
        } else {
            this.groupIndexes = null;
        }

        String regex = parser.getRegex();
        if (regex.endsWith("/")) {
            regex = regex.substring(0, regex.length() - 1);
        }
        this.regex = regex + URI_PATTERN_TAIL;
        this.pattern = Pattern.compile(this.regex);
    }


    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && getRegex().equals(((UriPattern)obj).getRegex());
    }


    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + regex.hashCode();
        return hash;
    }

    /**
     * Get the regex pattern.
     *
     * @return the regex pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Get the URI template as a String.
     *
     * @return the URI template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Get the regular expression.
     *
     * @return the regular expression
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Get the number of literal characters in the template.
     *
     * @return number of literal characters in the template
     */
    public int getNumberOfLiteralCharacters() {
        return numberOfCharacters;
    }

    /** @return list of names */
    public List<String> getParameterNames() {
        return parameterNames;
    }

    /**
     * Check is URI string match to pattern. If it is then fill given list by parameter value. Before coping value list
     * is cleared. List will be 1 greater then number of keys. It can be used for check is resource is matching to
     * requested. If resource is match the last element in list must be '/' or null.
     *
     * @param uri
     *         the URI string
     * @param parameters
     *         target list
     * @return true if URI string is match to pattern, false otherwise
     */
    public boolean match(String uri, List<String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("list is null");
        }

        if (uri == null || uri.isEmpty()) {
            return pattern == null;
        }
        if (pattern == null) {
            return false;
        }

        Matcher m = pattern.matcher(uri);
        if (!m.matches()) {
            return false;
        }

        parameters.clear();
        if (groupIndexes == null) {
            for (int i = 1; i <= m.groupCount(); i++) {
                parameters.add(m.group(i));
            }
        } else {
            for (int i = 0; i < groupIndexes.length - 1; i++) {
                parameters.add(m.group(groupIndexes[i]));
            }
        }
        return true;
    }


    public String toString() {
        return regex;
    }
}
