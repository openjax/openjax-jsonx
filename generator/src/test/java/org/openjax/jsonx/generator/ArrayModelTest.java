/* Copyright (c) 2018 OpenJAX
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

package org.openjax.jsonx.generator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openjax.jsonx.jsonx_0_9_8.xL3gluGCXYYJc;
import org.openjax.jsonx.jsonx_0_9_8.xL3gluGCXYYJc.$Member;
import org.openjax.jsonx.jsonx_0_9_8.xL3gluGCXYYJc.$Number;

public class ArrayModelTest {
  private static class Number extends $Number {
    private static final long serialVersionUID = 763116191728279846L;

    public Number() {
    }

    @SuppressWarnings("unused")
    public Number(final $Member inherits) {
      super(inherits);
    }

    @Override
    protected $Member inherits() {
      return this;
    }
  }

  @Test
  public void testGreatestCommonSuperObject() {
    final Registry registry = new Registry();
    final xL3gluGCXYYJc.Jsonx jsonx = new xL3gluGCXYYJc.Jsonx();
    jsonx.setPackage$(new xL3gluGCXYYJc.Jsonx.Package$(getClass().getPackageName()));

    final $Number number1 = new Number();
    number1.setName$(new $Number.Name$("integer1"));
    number1.setForm$(new $Number.Form$($Number.Form$.integer));
    final NumberModel model1 = NumberModel.reference(registry, null, number1);

    final $Number number2 = new Number();
    number2.setName$(new $Number.Name$("integer2"));
    number2.setForm$(new $Number.Form$($Number.Form$.integer));
    final NumberModel model2 = NumberModel.reference(registry, null, number2);

    Assert.assertEquals(registry.getType(List.class, BigInteger.class), ArrayModel.getGreatestCommonSuperType(registry, Arrays.asList(model1, model2)));
  }
}