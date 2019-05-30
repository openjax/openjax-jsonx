/* Copyright (c) 2019 JSONx
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.jsonx;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;
import org.libj.io.Readers;
import org.openjax.json.JsonReader;

public class AnyObjectTest {
  private static void testArray(final String fileName) throws DecodeException, IOException {
    try (final JsonReader reader = test(fileName)) {
      final List<?> array = JxDecoder.parseArray(AnyArray.class, reader);
      reader.reset();
      final String expected = Readers.readFully(reader);
      final String actual = JxEncoder.get().marshal(array, AnyArray.class);
      assertEquals(expected, actual);
    }
  }

  private static void testObject(final String fileName) throws DecodeException, IOException {
    try (final JsonReader reader = test(fileName)) {
      final AnyObject object = JxDecoder.parseObject(AnyObject.class, reader);
      reader.reset();
      final String expected = Readers.readFully(reader);
      final String actual = object.toString();
      assertEquals(expected, actual);
    }
  }

  private static JsonReader test(final String fileName) {
    return new JsonReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(fileName)));
  }

  @Test
  public void testObject() throws DecodeException, IOException {
    testObject("object.json");
  }

  @Test
  public void testArray() throws DecodeException, IOException {
    testObject("array.json");
  }

  @Test
  public void testColors() throws DecodeException, IOException {
    testObject("colors.json");
  }

  @Test
  public void testGeoIp() throws DecodeException, IOException {
    testObject("geoip.json");
  }

  @Test
  public void testProducts() throws DecodeException, IOException {
    testObject("products.json");
  }

  @Test
  public void testTwitter() throws DecodeException, IOException {
    testArray("twitter.json");
  }

  @Test
  public void testWordPress() throws DecodeException, IOException {
    testArray("wordpress.json");
  }

  @Test
  public void testYouTube() throws DecodeException, IOException {
    testObject("youtube.json");
  }
}