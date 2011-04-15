/**
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.everrest.core.impl.header;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.header.Ranges.Range;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RangeTest extends BaseTest
{

   /* Total length of content. */
   private final long length = 500;

   public void testOneRange()
   {
      Ranges ranges = Ranges.valueOf("bytes=49-100");
      assertEquals(1, ranges.getRanges().size());
      Range rangeItem = ranges.getRanges().get(0);
      assertTrue(rangeItem.validate(length));
      assertEquals(49, rangeItem.getStart());
      assertEquals(100, rangeItem.getEnd());
   }

   public void testOneRangeOverTotalLength()
   {
      Ranges ranges = Ranges.valueOf("bytes=49-1000");
      assertEquals(1, ranges.getRanges().size());
      Range rangeItem = ranges.getRanges().get(0);

      assertEquals(49, rangeItem.getStart());
      assertEquals(1000, rangeItem.getEnd());

      // validate range since end of range if over total length
      assertTrue(rangeItem.validate(length));

      assertEquals(49, rangeItem.getStart());
      // must be changed to the end of content
      assertEquals(499, rangeItem.getEnd());
   }

   public void testOneRangeNoStart()
   {
      Ranges ranges = Ranges.valueOf("bytes=-100");
      assertEquals(1, ranges.getRanges().size());
      Range rangeItem = ranges.getRanges().get(0);

      assertEquals(-100, rangeItem.getStart());
      assertEquals(-1, rangeItem.getEnd());

      // validate range since start of range is not set
      assertTrue(rangeItem.validate(length));

      // 100 bytes from the end
      assertEquals(400, rangeItem.getStart());
      // must be changed to the end of content
      assertEquals(499, rangeItem.getEnd());
   }

   public void testOneRangeNoEnd()
   {
      Ranges ranges = Ranges.valueOf("bytes=100-");
      assertEquals(1, ranges.getRanges().size());
      Range rangeItem = ranges.getRanges().get(0);

      assertEquals(100, rangeItem.getStart());
      assertEquals(-1, rangeItem.getEnd());

      // validate range since end of range is not set
      assertTrue(rangeItem.validate(length));

      assertEquals(100, rangeItem.getStart());
      // must be changed to the end of content
      assertEquals(499, rangeItem.getEnd());
   }

   public void testOneByte()
   {
      Ranges ranges = Ranges.valueOf("bytes=100-100");
      assertEquals(1, ranges.getRanges().size());
      Range rangeItem = ranges.getRanges().get(0);
      assertTrue(rangeItem.validate(length));
      assertEquals(100, rangeItem.getStart());
      assertEquals(100, rangeItem.getEnd());
   }

   public void testMultiRange()
   {
      Ranges ranges = Ranges.valueOf("bytes=49-100,150-250");
      assertEquals(2, ranges.getRanges().size());

      // first range
      Range rangeItem = ranges.getRanges().get(0);
      assertTrue(rangeItem.validate(length));
      assertEquals(49, rangeItem.getStart());
      assertEquals(100, rangeItem.getEnd());

      // second range
      rangeItem = ranges.getRanges().get(1);
      assertTrue(rangeItem.validate(length));
      assertEquals(150, rangeItem.getStart());
      assertEquals(250, rangeItem.getEnd());
   }

   public void testInvalidRange()
   {
      Ranges ranges = Ranges.valueOf("bytes=101-100");
      assertEquals(1, ranges.getRanges().size());
      Range rangeItem = ranges.getRanges().get(0);
      // start of range is over end of range
      assertFalse(rangeItem.validate(length));
      assertEquals(101, rangeItem.getStart());
      assertEquals(100, rangeItem.getEnd());
   }
}
