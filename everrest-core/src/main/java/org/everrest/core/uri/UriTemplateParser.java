/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.uri;

import org.everrest.core.impl.uri.UriComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class UriTemplateParser {
    /** Pattern for process URI parameters, for example /a/b/{x}/c . */
    public static final Pattern URI_PARAMETERS_PATTERN = Pattern.compile("\\{[^\\}^\\{]+\\}");

    /** Regex character, this characters in URI template will be escaped by addition '\'. */
    private static final String REGEX_CHARACTERS = ".?()";

    /** Should be added in regular expression instead URI parameter. */
    private static final String URI_PARAMETER_TEMPLATE = "[^/]+?";

    /** URI parameter names. */
    private List<String> names = new ArrayList<String>();

    /** Regular expression buffer. */
    private StringBuilder regex = new StringBuilder();

    /** Normalized template, whitespace must be removed. */
    private StringBuilder normalizedTemplate = new StringBuilder();

    /** Number of explicit characters in URI template, all characters except parameters. */
    private int numberOfCharacters = 0;

    /** Actual processed parameter name. */
    private String name;

    /** Indexes regular expression capturing group. */
    private List<Integer> groupIndexes = new ArrayList<Integer>();

    /**
     * @param template
     *         source URI template
     */
    public UriTemplateParser(String template) {
        Matcher m = URI_PARAMETERS_PATTERN.matcher(template);
        int start = 0;
        while (m.find()) {
            numberOfCharacters += addCharacter(template, start, m.start());
            parseRegex(template.substring(m.start() + 1, m.end() - 1));
            start = m.end();
        }

        numberOfCharacters += addCharacter(template, start, template.length());
    }

    /**
     * Get the number of literal characters in the template.
     *
     * @return number of literal characters in the template
     */
    public final int getNumberOfLiteralCharacters() {
        return numberOfCharacters;
    }

    /** @return list of names */
    public final List<String> getParameterNames() {
        return names;
    }

    /**
     * Get the regular expression.
     *
     * @return the regular expression
     */
    public final String getRegex() {
        return regex.toString();
    }

    /**
     * Get the URI template.
     *
     * @return the URI template
     */
    public final String getTemplate() {
        return normalizedTemplate.toString();
    }

    /** @return indexes of regular expression capturing group */
    public final int[] getGroupIndexes() {
        if (names.isEmpty()) {
            return null;
        }
        int[] indexes = new int[names.size() + 1];
        indexes[0] = 1;
        for (int i = 1; i < indexes.length; i++) {
            indexes[i] = indexes[i - 1] + groupIndexes.get(i - 1);
        }

        // Check are groups indexes goes one by one.
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] != i + 1) {
                return indexes;
            }
        }
        return null;
    }

    /**
     * Encode set of literal characters. Characters must not e double encoded.
     *
     * @param literalCharacters
     *         source string
     * @return encoded string
     */
    protected String encodeLiteralCharacters(String literalCharacters) {
        return UriComponent.recognizeEncode(literalCharacters, UriComponent.PATH, false);
    }

    /**
     * @param str
     *         part of source template between '{' and '}'
     */
    private void parseRegex(String str) {
        int length = str.length();
        int p = parseName(str, 0, length);
        String reg;
        if (p == length) {
            reg = URI_PARAMETER_TEMPLATE;
            addToTemplate(name);
        } else {
            reg = str.substring(p + 1).trim();
            if (reg.length() == 0) {
                reg = URI_PARAMETER_TEMPLATE;
                addToTemplate(name);
            } else {
                addToTemplate(name, reg);
            }
        }

        // Count how many groups has current part of template
        Pattern gp = Pattern.compile(reg);
        Matcher m = gp.matcher("");
        groupIndexes.add(m.groupCount() + 1);

        regex.append('(').append(reg).append(')');
    }

    /**
     * Add name of parameter to normalized template.
     *
     * @param name
     *         parameter name
     */
    private void addToTemplate(String name) {
        normalizedTemplate.append('{').append(name).append('}');
    }

    /**
     * Add name of parameter and regular expression corresponded to name to
     * normalized template.
     *
     * @param name
     *         parameter name
     * @param reg
     *         regular expression
     */
    private void addToTemplate(String name, String reg) {
        normalizedTemplate.append('{').append(name).append('}').append(':').append(reg);
    }

    /**
     * @param str
     *         Part of source URI template between '{' and '}'.
     * @param p
     *         start position
     * @param length
     *         string length
     * @return URI template parameter name
     */
    private int parseName(String str, int p, int length) {
        StringBuilder sb = new StringBuilder();
        for (; p < length; p++) {
            char ch = str.charAt(p);
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == '.') {
                sb.append(ch);
            } else if (ch == ':') {
                break;
            } else if (ch != ' ') // skip whitespace
            {
                throw new IllegalArgumentException("Wrong character at part " + str);
            }
        }
        name = sb.toString();
        // TODO remove restriction that not allowed have few path parameters with
        // the same name. This should be allowed but part of URI template also MUST
        // be the same. E.g. /a/{x}/b/{x} and /a/{x:\d+}/b/{x:\d+} must be allowed,
        // but /a/{x}/b/{x:\d+} is not allowed. This task is not high priority.
        if (names.contains(name)) {
            throw new IllegalArgumentException("URI template variables name " + name + " already registered.");
        }
        names.add(name);
        return p;
    }

    /**
     * Add explicit characters to normalized URI template.
     *
     * @param template
     *         source URI template
     * @param start
     *         start position for reading characters
     * @param end
     *         end position for reading characters
     * @return how many characters was red
     */
    private int addCharacter(String template, int start, int end) {
        String str = encodeLiteralCharacters(template.substring(start, end));
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            // check is character corresponds to regular expression character
            if (REGEX_CHARACTERS.indexOf(ch) != -1) {
                regex.append('\\');
            }

            regex.append(ch);
            normalizedTemplate.append(ch);
        }

        return end - start;
    }
}
