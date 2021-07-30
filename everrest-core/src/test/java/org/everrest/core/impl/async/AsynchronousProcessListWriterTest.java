/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.async;

import com.google.common.io.CharStreams;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static org.everrest.core.util.ParameterizedTypeImpl.newParameterizedType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsynchronousProcessListWriterTest {

    private AsynchronousProcessListWriter processListWriter;

    @Before
    public void setUp() throws Exception {
        processListWriter = new AsynchronousProcessListWriter();
    }

    @Test
    public void testIsWritable() throws Exception {
        assertTrue(processListWriter.isWriteable(List.class,
                                                 newParameterizedType(List.class, AsynchronousProcess.class),
                                                 new Annotation[0],
                                                 MediaType.TEXT_PLAIN_TYPE));
    }

    @Test
    public void writesListOfAsynchronousProcessAsPlainText() throws Exception {
        List<AsynchronousProcess> processes = newArrayList(new AsynchronousProcess("andrew", 1L, "/a", "running"),
                                                           new AsynchronousProcess("user", 2L, "/b", "done"));

        List<List<String>> expectedProcessesTable = newArrayList(newArrayList("USER", "ID", "STAT", "PATH"),
                                                                 newArrayList("andrew", "1", "running", "/a"),
                                                                 newArrayList("user", "2", "done", "/b"));

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        processListWriter.writeTo(processes, List.class,
                                  newParameterizedType(List.class, AsynchronousProcess.class),
                                  new Annotation[0],
                                  MediaType.TEXT_PLAIN_TYPE,
                                  new MultivaluedHashMap<>(),
                                  bout);

        List<String> lines = CharStreams.readLines(new StringReader(bout.toString()));
        assertEquals(3, lines.size());

        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s+(\\w+)\\s+(/?\\w+)");

        List<List<String>> processesTable = newArrayList();
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            assertTrue(String.format("String '%s' is not matched to pattern", line), matcher.matches());
            processesTable.add(getAllGroups(matcher));
        }

        assertEquals(expectedProcessesTable, processesTable);
    }

    private List<String> getAllGroups(Matcher matcher) {
        final int groupCount = matcher.groupCount();
        List<String> groups = new ArrayList<>(groupCount);
        for (int i = 1; i <= groupCount; i++) {
            groups.add(matcher.group(i));
        }
        return groups;
    }
}
