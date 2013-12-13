/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.impl.provider.json;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author andrew00x */
public class ObjectBuilderTest extends JsonTest {
    private ArrayList<Book> sourceCollection;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceCollection = new ArrayList<Book>(3);
        sourceCollection.add(junitBook);
        sourceCollection.add(csharpBook);
        sourceCollection.add(javaScriptBook);
    }

    public void testCollectionArrayList() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getArrayList().size());
        assertTrue(o.getArrayList().get(0).equals(sourceCollection.get(0)));
        assertTrue(o.getArrayList().get(1).equals(sourceCollection.get(1)));
        assertTrue(o.getArrayList().get(2).equals(sourceCollection.get(2)));
    }

    public void testCollectionVector() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getVector().size());
        assertTrue(o.getVector().get(0).equals(sourceCollection.get(0)));
        assertTrue(o.getVector().get(1).equals(sourceCollection.get(1)));
        assertTrue(o.getVector().get(2).equals(sourceCollection.get(2)));
    }

    public void testCollectionLinkedList() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getLinkedList().size());
        assertTrue(o.getLinkedList().get(0).equals(sourceCollection.get(0)));
        assertTrue(o.getLinkedList().get(1).equals(sourceCollection.get(1)));
        assertTrue(o.getLinkedList().get(2).equals(sourceCollection.get(2)));
    }

    public void testCollectionLinkedHashSet() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getLinkedHashSet().size());
        assertTrue(o.getLinkedHashSet().contains(sourceCollection.get(0)));
        assertTrue(o.getLinkedHashSet().contains(sourceCollection.get(1)));
        assertTrue(o.getLinkedHashSet().contains(sourceCollection.get(2)));
    }

    public void testCollectionHashSet() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getHashSet().size());
        assertTrue(o.getHashSet().contains(sourceCollection.get(0)));
        assertTrue(o.getHashSet().contains(sourceCollection.get(1)));
        assertTrue(o.getHashSet().contains(sourceCollection.get(2)));
    }

    public void testCollectionList() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getList().size());
        assertTrue(o.getList().get(0).equals(sourceCollection.get(0)));
        assertTrue(o.getList().get(1).equals(sourceCollection.get(1)));
        assertTrue(o.getList().get(2).equals(sourceCollection.get(2)));
    }

    public void testCollectionListList() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getListList().size());
        assertTrue(o.getListList().get(0).equals(Arrays.asList(sourceCollection.get(0))));
        assertTrue(o.getListList().get(1).equals(Arrays.asList(sourceCollection.get(1))));
        assertTrue(o.getListList().get(2).equals(Arrays.asList(sourceCollection.get(2))));
    }

    public void testCollectionListMap() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(1, o.getListMap().size());
        assertTrue(o.getListMap().get(0).get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getListMap().get(0).get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getListMap().get(0).get("JavaScript").equals(sourceCollection.get(2)));
    }

    public void testCollectionSet() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getSet().size());
        assertTrue(o.getSet().contains(sourceCollection.get(0)));
        assertTrue(o.getSet().contains(sourceCollection.get(1)));
        assertTrue(o.getSet().contains(sourceCollection.get(2)));
    }

    public void testCollectionQueue() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getQueue().size());
        assertTrue(o.getQueue().contains(sourceCollection.get(0)));
        assertTrue(o.getQueue().contains(sourceCollection.get(1)));
        assertTrue(o.getQueue().contains(sourceCollection.get(2)));
    }

    public void testCollectionCollection() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getCollection().size());
        assertTrue(o.getCollection().contains(sourceCollection.get(0)));
        assertTrue(o.getCollection().contains(sourceCollection.get(1)));
        assertTrue(o.getCollection().contains(sourceCollection.get(2)));
    }

    public void testCollectionArray() throws Exception {
        JsonParser jsonParser = new JsonParser();
        // check restore different type of Collection
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("CollectionTest.json")));
        JsonValue jsonValue = jsonParser.getJsonObject();

        JavaCollectionBean o = ObjectBuilder.createObject(JavaCollectionBean.class, jsonValue);

        assertEquals(3, o.getArray().length);
        assertTrue(o.getArray()[0].equals(sourceCollection.get(0)));
        assertTrue(o.getArray()[1].equals(sourceCollection.get(1)));
        assertTrue(o.getArray()[2].equals(sourceCollection.get(2)));
    }

    public void testMap2() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("MapTest.json")));
        JsonValue jv = jsonParser.getJsonObject();
        JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

        assertTrue(o.getMap().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getMap().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getMap().get("JavaScript").equals(sourceCollection.get(2)));

        assertTrue(o.getHashMap().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getHashMap().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getHashMap().get("JavaScript").equals(sourceCollection.get(2)));

        assertTrue(o.getHashtable().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getHashtable().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getHashtable().get("JavaScript").equals(sourceCollection.get(2)));

        assertTrue(o.getLinkedHashMap().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getLinkedHashMap().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getLinkedHashMap().get("JavaScript").equals(sourceCollection.get(2)));

    }

    public void testMapMap() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("MapTest.json")));
        JsonValue jv = jsonParser.getJsonObject();
        JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

        assertTrue(o.getMap().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getMap().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getMap().get("JavaScript").equals(sourceCollection.get(2)));
    }

    public void testMapHashMap() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("MapTest.json")));
        JsonValue jv = jsonParser.getJsonObject();
        JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

        assertTrue(o.getHashMap().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getHashMap().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getHashMap().get("JavaScript").equals(sourceCollection.get(2)));
    }

    public void testMapHashtable() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("MapTest.json")));
        JsonValue jv = jsonParser.getJsonObject();
        JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

        assertTrue(o.getHashtable().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getHashtable().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getHashtable().get("JavaScript").equals(sourceCollection.get(2)));
    }

    public void testMapLinkedHashMap() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("MapTest.json")));
        JsonValue jv = jsonParser.getJsonObject();
        JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

        assertTrue(o.getLinkedHashMap().get("JUnit").equals(sourceCollection.get(0)));
        assertTrue(o.getLinkedHashMap().get("C#").equals(sourceCollection.get(1)));
        assertTrue(o.getLinkedHashMap().get("JavaScript").equals(sourceCollection.get(2)));
    }

    public void testBean() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("BookStorage.json")));
        JsonValue jv = jsonParser.getJsonObject();
        BookStorage o = ObjectBuilder.createObject(BookStorage.class, jv);
        assertTrue(o.getBooks().get(0).equals(sourceCollection.get(0)));
        assertTrue(o.getBooks().get(1).equals(sourceCollection.get(1)));
        assertTrue(o.getBooks().get(2).equals(sourceCollection.get(2)));
    }

    public void testMapMapList() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("MapTest.json")));
        JsonValue jv = jsonParser.getJsonObject();
        JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

        assertTrue(o.getMapList().get("JUnit").equals(Arrays.asList(sourceCollection.get(0))));
        assertTrue(o.getMapList().get("C#").equals(Arrays.asList(sourceCollection.get(1))));
        assertTrue(o.getMapList().get("JavaScript").equals(Arrays.asList(sourceCollection.get(2))));
    }

    public void testMapMapMap() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("MapTest.json")));
        JsonValue jv = jsonParser.getJsonObject();
        JavaMapBean o = ObjectBuilder.createObject(JavaMapBean.class, jv);

        assertTrue(o.getMapMap().get("JUnit").get("_").equals(sourceCollection.get(0)));
        assertTrue(o.getMapMap().get("C#").get("_").equals(sourceCollection.get(1)));
        assertTrue(o.getMapMap().get("JavaScript").get("_").equals(sourceCollection.get(2)));
    }

    public void testEnumSerialization() throws Exception {
        String source =
                "{\"countList\":[\"ONE\",\"TWO\",\"TREE\"], \"name\":\"andrew\",\"count\":\"TREE\",\"counts\":[\"TWO\",\"TREE\"]}";
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new ByteArrayInputStream(source.getBytes()));
        JsonValue jsonValue = jsonParser.getJsonObject();
        //System.out.println(jsonValue);

        BeanWithSimpleEnum o = ObjectBuilder.createObject(BeanWithSimpleEnum.class, jsonValue);

        assertEquals("andrew", o.getName());

        assertEquals(StringEnum.TREE, o.getCount());

        StringEnum[] counts = o.getCounts();
        assertEquals(2, counts.length);

        List<StringEnum> tmp = Arrays.asList(counts);
        assertTrue(tmp.contains(StringEnum.TWO));
        assertTrue(tmp.contains(StringEnum.TREE));

        tmp = o.getCountList();
        assertEquals(3, tmp.size());
        assertTrue(tmp.contains(StringEnum.ONE));
        assertTrue(tmp.contains(StringEnum.TWO));
        assertTrue(tmp.contains(StringEnum.TREE));
    }

    public void testEnumSerialization2() throws Exception {
        String source = "{\"book\":\"BEGINNING_C\"}";
        JsonParser parser = new JsonParser();
        parser.parse(new ByteArrayInputStream(source.getBytes()));
        JsonValue jsonValue = parser.getJsonObject();
        //System.out.println(jsonValue);
        BeanWithBookEnum o = ObjectBuilder.createObject(BeanWithBookEnum.class, jsonValue);
        assertEquals(BookEnum.BEGINNING_C, o.getBook());
    }

    public void testClass() throws Exception {
        String source = "{\"klass\":\"" + ForTestClass001.class.getName() + "\"}";
        JsonParser parser = new JsonParser(new JsonHandler());
        parser.parse(new ByteArrayInputStream(source.getBytes()));
        JsonValue jsonValue = parser.getJsonObject();
        //System.out.println(jsonValue);
        ClassTransfBean o = ObjectBuilder.createObject(ClassTransfBean.class, jsonValue);
        assertEquals(ForTestClass001.class, o.getKlass());
    }

    public static class ForTestClass001 {
    }

    public void testConvertFromStringValue() throws Exception {
        String jsonString =
                "{\"b\":\"1\", \"s\":\"2\" , \"i\":\"3\", \"l\":\"4\",\"f\":\"1.05\",\"d\":\"1.1\",\"bool\":\"true\"}";
        JsonParser parser = new JsonParser(new JsonHandler());
        parser.parse(new ByteArrayInputStream(jsonString.getBytes()));
        JsonValue jsonValue = parser.getJsonObject();
        //System.out.println(jsonValue);
        ConvertFromStringValueBean o = ObjectBuilder.createObject(ConvertFromStringValueBean.class, jsonValue);
        assertEquals(1, o.getB());
        assertEquals(2, o.getS());
        assertEquals(3, o.getI());
        assertEquals(4L, o.getL());
        assertEquals(1.05F, o.getF());
        assertEquals(1.1D, o.getD());
        assertEquals(true, o.isBool());
    }

    public void testInterfaces() throws Exception {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("BookStorage.json")));
        JsonValue jv = jsonParser.getJsonObject();
        IBookStorage o = ObjectBuilder.createObject(IBookStorage.class, jv);
        assertEquals(3, o.getBooks().size());
        assertEquals(sourceCollection.get(0).getIsdn(), o.getBooks().get(0).getIsdn());
        assertEquals(sourceCollection.get(1).getIsdn(), o.getBooks().get(1).getIsdn());
        assertEquals(sourceCollection.get(2).getIsdn(), o.getBooks().get(2).getIsdn());
    }

    public static class ConvertFromStringValueBean {
        private byte    b;
        private short   s;
        private int     i;
        private long    l;
        private float   f;
        private double  d;
        private boolean bool;

        public byte getB() {
            return b;
        }

        public void setB(byte b) {
            this.b = b;
        }

        public short getS() {
            return s;
        }

        public void setS(short s) {
            this.s = s;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public long getL() {
            return l;
        }

        public void setL(long l) {
            this.l = l;
        }

        public float getF() {
            return f;
        }

        public void setF(float f) {
            this.f = f;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }
    }
}
