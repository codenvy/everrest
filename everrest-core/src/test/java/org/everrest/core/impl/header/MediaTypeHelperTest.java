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
package org.everrest.core.impl.header;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.everrest.core.util.MediaTypeComparator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** @author andrew00x */
@RunWith(DataProviderRunner.class)
public class MediaTypeHelperTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @DataProvider
  public static Object[][] forMatchingOfMediaTypes() {
    return new Object[][] {
      {
        new MediaType("application", "atom+xml"),
        new MediaType("application", "xhtml+xml"),
        false,
        false
      },
      {
        new MediaType("application", "atom+*"),
        new MediaType("application", "atom+xml"),
        true,
        false
      },
      {
        new MediaType("application", "*+xml"), new MediaType("application", "atom+xml"), true, false
      },
      {new MediaType("application", "*+xml"), new MediaType("application", "xml"), true, false},
      {new MediaType("application", "xml"), new MediaType("application", "*"), false, true},
      {new MediaType("application", "xml"), new MediaType("*", "bla-bla"), false, true},
      {new MediaType("application", "xml"), new MediaType("*", "*"), false, true}
    };
  }

  @UseDataProvider("forMatchingOfMediaTypes")
  @Test
  public void matchingOfMediaTypes(
      MediaType mediaTypeOne,
      MediaType mediaTypeTwo,
      boolean oneMatchedToTwo,
      boolean twoMatchedToOne) {
    assertEquals(oneMatchedToTwo, MediaTypeHelper.isMatched(mediaTypeOne, mediaTypeTwo));
    assertEquals(twoMatchedToOne, MediaTypeHelper.isMatched(mediaTypeTwo, mediaTypeOne));
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionWhenFirstMediaTypeForMatchingIsNull() {
    MediaTypeHelper.isMatched(null, new MediaType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionWhenSecondMediaTypeForMatchingIsNull() {
    MediaTypeHelper.isMatched(new MediaType(), null);
  }

  @DataProvider
  public static Object[][] forCompatibilityOfMediaTypes() {
    return new Object[][] {
      {
        new MediaType("application", "atom+xml"),
        new MediaType("application", "xhtml+xml"),
        false,
        false
      },
      {
        new MediaType("application", "atom+*"), new MediaType("application", "atom+xml"), true, true
      },
      {new MediaType("application", "*+xml"), new MediaType("application", "xml"), true, true},
      {new MediaType("application", "*+xml"), new MediaType("application", "atom+xml"), true, true},
      {new MediaType("application", "xml"), new MediaType("application", "*"), true, true},
      {new MediaType("application", "xml"), new MediaType("*", "bla-bla"), true, true},
      {new MediaType("application", "xml"), new MediaType("*", "*"), true, true}
    };
  }

  @UseDataProvider("forCompatibilityOfMediaTypes")
  @Test
  public void compatibilityOfMediaTypes(
      MediaType mediaTypeOne,
      MediaType mediaTypeTwo,
      boolean oneCompatibleToTwo,
      boolean twoCompatibleToOne) {
    assertEquals(oneCompatibleToTwo, MediaTypeHelper.isCompatible(mediaTypeOne, mediaTypeTwo));
    assertEquals(twoCompatibleToOne, MediaTypeHelper.isCompatible(mediaTypeTwo, mediaTypeOne));
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionWhenFirstMediaTypeForCheckingCompatibilityIsNull() {
    MediaTypeHelper.isCompatible(null, new MediaType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionWhenSecondMediaTypeForCheckingCompatibilityIsNull() {
    MediaTypeHelper.isCompatible(new MediaType(), null);
  }

  @DataProvider
  public static Object[][] forFindsFirstCompatibleAcceptMediaType() {
    return new Object[][] {
      {newArrayList(), newArrayList(), null},
      {
        newArrayList(AcceptMediaType.valueOf("text/plain")),
        newArrayList(MediaType.valueOf("text/plain")),
        AcceptMediaType.valueOf("text/plain")
      },
      {
        newArrayList(AcceptMediaType.valueOf("text/*")),
        newArrayList(MediaType.valueOf("text/plain")),
        AcceptMediaType.valueOf("text/*")
      },
      {
        newArrayList(AcceptMediaType.valueOf("text/plain"), AcceptMediaType.valueOf("text/*")),
        newArrayList(MediaType.valueOf("text/xml"), MediaType.valueOf("text/plain")),
        AcceptMediaType.valueOf("text/plain")
      },
      {
        newArrayList(AcceptMediaType.valueOf("*/*"), AcceptMediaType.valueOf("text/*")),
        newArrayList(MediaType.valueOf("text/xml"), MediaType.valueOf("text/plain")),
        AcceptMediaType.valueOf("*/*")
      },
      {
        newArrayList(
            AcceptMediaType.valueOf("application/xml"), AcceptMediaType.valueOf("text/xml")),
        newArrayList(MediaType.valueOf("text/plain")),
        null
      }
    };
  }

  @UseDataProvider("forFindsFirstCompatibleAcceptMediaType")
  @Test
  public void findsFirstCompatibleAcceptMediaType(
      List<AcceptMediaType> acceptMediaTypes,
      List<MediaType> producedByResource,
      AcceptMediaType expectedResult) {
    AcceptMediaType result =
        MediaTypeHelper.findFistCompatibleAcceptMediaType(acceptMediaTypes, producedByResource);
    assertEquals(expectedResult, result);
  }

  @DataProvider
  public static Object[][] forCreatesDescendingMediaTypeIterator() {
    return new Object[][] {
      {
        new MediaType("application", "xml"),
        newArrayList(
            new MediaType("application", "xml"),
            new MediaType("application", "*+xml"),
            new MediaType("application", "*"),
            new MediaType("*", "*"))
      },
      {
        new MediaType("application", "*+xml"),
        newArrayList(
            new MediaType("application", "*+xml"),
            new MediaType("application", "*"),
            new MediaType("*", "*"))
      },
      {
        new MediaType("application", "*"),
        newArrayList(new MediaType("application", "*"), new MediaType("*", "*"))
      },
      {new MediaType("*", "*"), newArrayList(new MediaType("*", "*"))},
    };
  }

  @UseDataProvider("forCreatesDescendingMediaTypeIterator")
  @Test
  public void createsDescendingMediaTypeIterator(
      MediaType startPoint, List<MediaType> expectedResult) {
    Iterator<MediaType> iterator = MediaTypeHelper.createDescendingMediaTypeIterator(startPoint);
    assertEquals(expectedResult, newArrayList(iterator));
  }

  @Test
  public void descendingMediaTypeIteratorThrowsExceptionWhenHasNoMoreElements() {
    Iterator<MediaType> iterator =
        MediaTypeHelper.createDescendingMediaTypeIterator(new MediaType("*", "*"));
    iterator.next();
    assertFalse(iterator.hasNext());
    thrown.expect(NoSuchElementException.class);
    iterator.next();
  }

  @Test
  public void removingIsNotSupportedByDescendingMediaTypeIterator() {
    Iterator<MediaType> iterator =
        MediaTypeHelper.createDescendingMediaTypeIterator(new MediaType("*", "*"));
    iterator.next();
    thrown.expect(UnsupportedOperationException.class);
    iterator.remove();
  }

  @DataProvider
  public static Object[][] forTestMediaTypeComparator() {
    return new Object[][] {
      {new MediaType("*", "*"), new MediaType("*", "*"), 0},
      {new MediaType("application", "xml"), new MediaType("*", "*"), -1},
      {new MediaType("application", "*"), new MediaType("*", "*"), -1},
      {new MediaType("application", "xml"), new MediaType("application", "*"), -1},
      {new MediaType("*", "*"), new MediaType("application", "xml"), 1},
      {new MediaType("*", "*"), new MediaType("application", "*"), 1},
      {new MediaType("application", "*"), new MediaType("application", "xml"), 1},
      {new MediaType("application", "xml"), new MediaType("application", "*+xml"), -1},
      {new MediaType("application", "*+xml"), new MediaType("application", "xml"), 1},
      {new MediaType("application", "xml"), new MediaType("application", "atom+xml"), -1},
      {new MediaType("application", "atom+xml"), new MediaType("application", "xml"), 1},
      {new MediaType("application", "atom+*"), new MediaType("application", "atom+xml"), 1},
      {new MediaType("application", "atom+xml"), new MediaType("application", "atom+*"), -1},
      {new MediaType("application", "xml+*"), new MediaType("application", "*+xml"), -1},
      {new MediaType("application", "*+xml"), new MediaType("application", "xml+*"), 1}
    };
  }

  @UseDataProvider("forTestMediaTypeComparator")
  @Test
  public void testMediaTypeComparator(
      MediaType mediaTypeOne, MediaType mediaTypeTwo, int expectedResult) {
    assertEquals(expectedResult, new MediaTypeComparator().compare(mediaTypeOne, mediaTypeTwo));
  }

  @DataProvider
  public static Object[][] forCreatesListOfConsumedMediaTypes() {
    return new Object[][] {
      {null, newArrayList(new MediaType("*", "*"))},
      {createConsumes("*/*"), newArrayList(new MediaType("*", "*"))},
      {
        createConsumes("*/*", "text/plain", "text/*"),
        newArrayList(
            new MediaType("text", "plain"), new MediaType("text", "*"), new MediaType("*", "*"))
      }
    };
  }

  private static Consumes createConsumes(String... mediaTypes) {
    Consumes consumes = mock(Consumes.class);
    when(consumes.value()).thenReturn(mediaTypes);
    return consumes;
  }

  @UseDataProvider("forCreatesListOfConsumedMediaTypes")
  @Test
  public void createsListOfConsumedMediaTypes(Consumes consumes, List<MediaType> expectedResult) {
    assertEquals(expectedResult, MediaTypeHelper.createConsumesList(consumes));
  }

  @DataProvider
  public static Object[][] forCreatesListOfProducedMediaTypes() {
    return new Object[][] {
      {null, newArrayList(new MediaType("*", "*"))},
      {createProduces("*/*"), newArrayList(new MediaType("*", "*"))},
      {
        createProduces("*/*", "text/plain", "text/*"),
        newArrayList(
            new MediaType("text", "plain"), new MediaType("text", "*"), new MediaType("*", "*"))
      }
    };
  }

  private static Produces createProduces(String... mediaTypes) {
    Produces produces = mock(Produces.class);
    when(produces.value()).thenReturn(mediaTypes);
    return produces;
  }

  @UseDataProvider("forCreatesListOfProducedMediaTypes")
  @Test
  public void createsListOfProducedMediaTypes(Produces produces, List<MediaType> expectedResult) {
    assertEquals(expectedResult, MediaTypeHelper.createProducesList(produces));
  }

  @DataProvider
  public static Object[][] forTestIsConsumes() {
    return new Object[][] {
      {newArrayList(new MediaType("text", "*")), new MediaType("text", "plain"), true},
      {newArrayList(new MediaType("text", "plain")), new MediaType("text", "plain"), true},
      {newArrayList(new MediaType("*", "*")), new MediaType("text", "plain"), true},
      {newArrayList(new MediaType("text", "*")), new MediaType("*", "*"), false},
      {newArrayList(new MediaType("text", "plain")), new MediaType("text", "*"), false}
    };
  }

  @UseDataProvider("forTestIsConsumes")
  @Test
  public void testIsConsumes(
      List<MediaType> mediaTypes, MediaType mediaTypeForConsuming, boolean expectedResult) {
    assertEquals(expectedResult, MediaTypeHelper.isConsume(mediaTypes, mediaTypeForConsuming));
  }
}
