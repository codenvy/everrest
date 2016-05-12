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
package org.everrest.core.impl.provider.json.tst;

public class BeanWithPrimitiveFields {
    public static BeanWithPrimitiveFields createBeanWithPrimitiveFields() {
        BeanWithPrimitiveFields beanWithPrimitiveFields = new BeanWithPrimitiveFields();
        beanWithPrimitiveFields.aByte = 1;
        beanWithPrimitiveFields.aShort = 2;
        beanWithPrimitiveFields.anInt = 3;
        beanWithPrimitiveFields.aLong = 4L;
        beanWithPrimitiveFields.aFloat = 5.5F;
        beanWithPrimitiveFields.aDouble = 7.7;
        beanWithPrimitiveFields.aBoolean = true;
        beanWithPrimitiveFields.aChar = 'a';
        beanWithPrimitiveFields.aString = "JSON";

        beanWithPrimitiveFields.aBByte = 11;
        beanWithPrimitiveFields.aSShort = 22;
        beanWithPrimitiveFields.anInteger = 33;
        beanWithPrimitiveFields.aLLong = 44L;
        beanWithPrimitiveFields.aFFloat = 55.5F;
        beanWithPrimitiveFields.aDDouble = 77.7;
        beanWithPrimitiveFields.aBBoolean = true;
        beanWithPrimitiveFields.aCharacter = 'A';

        beanWithPrimitiveFields.bytes = new byte[]{1, 2};
        beanWithPrimitiveFields.shorts = new short[]{2, 3};
        beanWithPrimitiveFields.ints = new int[]{3, 4};
        beanWithPrimitiveFields.longs = new long[]{4L, 5L};
        beanWithPrimitiveFields.floats = new float[]{4.4F, 5.5F};
        beanWithPrimitiveFields.doubles = new double[]{5.5, 7.7};
        beanWithPrimitiveFields.booleans = new boolean[]{true, false};
        beanWithPrimitiveFields.chars = new char[]{'a', 'b'};

        beanWithPrimitiveFields.bBytes = new Byte[]{1, 2};
        beanWithPrimitiveFields.sShorts = new Short[]{2, 3};
        beanWithPrimitiveFields.integers = new Integer[]{3, 4};
        beanWithPrimitiveFields.lLongs = new Long[]{4L, 5L};
        beanWithPrimitiveFields.fFloats = new Float[]{4.4F, 5.5F};
        beanWithPrimitiveFields.dDoubles = new Double[]{5.5, 7.7};
        beanWithPrimitiveFields.bBooleans = new Boolean[]{true, false};
        beanWithPrimitiveFields.characters = new Character[]{'a', 'b'};
        beanWithPrimitiveFields.strings = new String[]{"test", "json"};

        return beanWithPrimitiveFields;
    }

    private byte    aByte;
    private short   aShort;
    private int     anInt;
    private long    aLong;
    private float   aFloat;
    private double  aDouble;
    private boolean aBoolean;
    private char    aChar;
    private String  aString;

    private Byte      aBByte;
    private Short     aSShort;
    private Integer   anInteger;
    private Long      aLLong;
    private Float     aFFloat;
    private Double    aDDouble;
    private Boolean   aBBoolean;
    private Character aCharacter;

    private byte[]    bytes;
    private short[]   shorts;
    private int[]     ints;
    private long[]    longs;
    private float[]   floats;
    private double[]  doubles;
    private boolean[] booleans;
    private char[]    chars;

    private Byte[]      bBytes;
    private Short[]     sShorts;
    private Integer[]   integers;
    private Long[]      lLongs;
    private Float[]     fFloats;
    private Double[]    dDoubles;
    private Boolean[]   bBooleans;
    private Character[] characters;
    private String[]    strings;

    public Boolean getaBBoolean() {
        return aBBoolean;
    }

    public void setaBBoolean(Boolean aBBoolean) {
        this.aBBoolean = aBBoolean;
    }

    public Byte getaBByte() {
        return aBByte;
    }

    public void setaBByte(Byte aBByte) {
        this.aBByte = aBByte;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public byte getaByte() {
        return aByte;
    }

    public void setaByte(byte aByte) {
        this.aByte = aByte;
    }

    public char getaChar() {
        return aChar;
    }

    public void setaChar(char aChar) {
        this.aChar = aChar;
    }

    public Character getaCharacter() {
        return aCharacter;
    }

    public void setaCharacter(Character aCharacter) {
        this.aCharacter = aCharacter;
    }

    public Double getaDDouble() {
        return aDDouble;
    }

    public void setaDDouble(Double aDDouble) {
        this.aDDouble = aDDouble;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public Float getaFFloat() {
        return aFFloat;
    }

    public void setaFFloat(Float aFFloat) {
        this.aFFloat = aFFloat;
    }

    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public Long getaLLong() {
        return aLLong;
    }

    public void setaLLong(Long aLLong) {
        this.aLLong = aLLong;
    }

    public long getaLong() {
        return aLong;
    }

    public void setaLong(long aLong) {
        this.aLong = aLong;
    }

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
        this.anInt = anInt;
    }

    public Integer getAnInteger() {
        return anInteger;
    }

    public void setAnInteger(Integer anInteger) {
        this.anInteger = anInteger;
    }

    public short getaShort() {
        return aShort;
    }

    public void setaShort(short aShort) {
        this.aShort = aShort;
    }

    public Short getaSShort() {
        return aSShort;
    }

    public void setaSShort(Short aSShort) {
        this.aSShort = aSShort;
    }

    public Boolean[] getbBooleans() {
        return bBooleans;
    }

    public void setbBooleans(Boolean[] bBooleans) {
        this.bBooleans = bBooleans;
    }

    public Byte[] getbBytes() {
        return bBytes;
    }

    public void setbBytes(Byte[] bBytes) {
        this.bBytes = bBytes;
    }

    public boolean[] getBooleans() {
        return booleans;
    }

    public void setBooleans(boolean[] booleans) {
        this.booleans = booleans;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Character[] getCharacters() {
        return characters;
    }

    public void setCharacters(Character[] characters) {
        this.characters = characters;
    }

    public char[] getChars() {
        return chars;
    }

    public void setChars(char[] chars) {
        this.chars = chars;
    }

    public Double[] getdDoubles() {
        return dDoubles;
    }

    public void setdDoubles(Double[] dDoubles) {
        this.dDoubles = dDoubles;
    }

    public double[] getDoubles() {
        return doubles;
    }

    public void setDoubles(double[] doubles) {
        this.doubles = doubles;
    }

    public Float[] getfFloats() {
        return fFloats;
    }

    public void setfFloats(Float[] fFloats) {
        this.fFloats = fFloats;
    }

    public float[] getFloats() {
        return floats;
    }

    public void setFloats(float[] floats) {
        this.floats = floats;
    }

    public Integer[] getIntegers() {
        return integers;
    }

    public void setIntegers(Integer[] integers) {
        this.integers = integers;
    }

    public int[] getInts() {
        return ints;
    }

    public void setInts(int[] ints) {
        this.ints = ints;
    }

    public Long[] getlLongs() {
        return lLongs;
    }

    public void setlLongs(Long[] lLongs) {
        this.lLongs = lLongs;
    }

    public long[] getLongs() {
        return longs;
    }

    public void setLongs(long[] longs) {
        this.longs = longs;
    }

    public short[] getShorts() {
        return shorts;
    }

    public void setShorts(short[] shorts) {
        this.shorts = shorts;
    }

    public Short[] getsShorts() {
        return sShorts;
    }

    public void setsShorts(Short[] sShorts) {
        this.sShorts = sShorts;
    }

    public String getaString() {
        return aString;
    }

    public void setaString(String aString) {
        this.aString = aString;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }
}
