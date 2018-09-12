/* Copyright (c) 2018 lib4j
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

package org.libx4j.jsonx.runtime;

import java.lang.reflect.Field;

public class BooleanSpec extends PrimitiveSpec<Boolean> {
  public BooleanSpec(final Field field, final BooleanProperty property) {
    super(field, property.name(), property.use());
  }

  @Override
  public boolean test(final char firstChar) {
    return firstChar == 't' || firstChar == 'f';
  }

  @Override
  public String validate(final String json) {
    return "true".equals(json) || "false".equals(json) ? null : "Not a valid boolean token: " + json;
  }

  @Override
  public Boolean decode(final String json) {
    return Boolean.valueOf(json);
  }

  @Override
  public String elementName() {
    return "boolean";
  }
}