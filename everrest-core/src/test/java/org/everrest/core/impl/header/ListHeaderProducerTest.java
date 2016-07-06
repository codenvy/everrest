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
package org.everrest.core.impl.header;

import com.google.common.collect.ImmutableMap;

import org.everrest.core.header.QualityValue;
import org.everrest.core.impl.header.ListHeaderProducer.ListItemFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListHeaderProducerTest {
    private final String                    header                      = "b,a,d,c";
    private final String                    headerWithSpaces            = " b\t, a , d,  c   ";
    private final List<QualityValue>        expectedQualityValueList    = newArrayList(mockQualityValue("a", 1.0f),
                                                                                       mockQualityValue("b", 0.7f),
                                                                                       mockQualityValue("c", 0.5f),
                                                                                       mockQualityValue("d", 0.3f));
    private final Map<String, QualityValue> headerItemToQualityValueMap = ImmutableMap.of("a", expectedQualityValueList.get(0),
                                                                                          "b", expectedQualityValueList.get(1),
                                                                                          "c", expectedQualityValueList.get(2),
                                                                                          "d", expectedQualityValueList.get(3));

    private ListHeaderProducer<QualityValue> listHeaderProducer;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        ListItemFactory<QualityValue> itemFactory = mock(ListItemFactory.class);
        when(itemFactory.createItem(anyString())).thenAnswer(invocation -> {
            String value = ((String)invocation.getArguments()[0]).trim();
            return headerItemToQualityValueMap.get(value);
        });
        listHeaderProducer = new ListHeaderProducer(itemFactory);
    }

    private QualityValue mockQualityValue(String value, Float qValue) {
        QualityValue qualityValue = mock(QualityValue.class);
        when(qualityValue.getQvalue()).thenReturn(qValue);
        when(qualityValue.toString()).thenReturn(value);
        return qualityValue;
    }

    @Test
    public void parsesGivenStringToSortedListOfQualityValues() {
        List<QualityValue> qualitySortedList = listHeaderProducer.createQualitySortedList(header);
        assertEquals(expectedQualityValueList, qualitySortedList);
    }

    @Test
    public void parsesGivenStringWithSpacesToSortedListOfQualityValues() {
        List<QualityValue> qualitySortedList = listHeaderProducer.createQualitySortedList(headerWithSpaces);
        assertEquals(expectedQualityValueList, qualitySortedList);
    }
}