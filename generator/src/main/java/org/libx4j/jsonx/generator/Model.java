/* Copyright (c) 2017 lib4j
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

package org.libx4j.jsonx.generator;

import org.libx4j.jsonx.jsonx_0_9_8.xL2gluGCXYYJc.$MaxCardinality;
import org.libx4j.jsonx.jsonx_0_9_8.xL2gluGCXYYJc.$Member;
import org.w3.www._2001.XMLSchema.yAA.$NonNegativeInteger;

abstract class Model extends Element {
  public Model(final Member owner, final String name, final Boolean nullable, final Boolean required, final Integer minOccurs, final Integer maxOccurs, final String doc) {
    super(owner, name, nullable, required, minOccurs, maxOccurs, doc, Kind.TEMPLATE);
  }

  public Model(final Member owner, final $Member binding, final String name, final Boolean nullable, final boolean required) {
    super(owner, name, nullable, required, binding.getDoc$() == null ? null : binding.getDoc$().text());
  }

  public Model(final Member owner, final $Member binding, final String name, final Boolean nullable, final $NonNegativeInteger minOccurs, final $MaxCardinality maxOccurs) {
    super(owner, binding, name, nullable, minOccurs, maxOccurs);
  }

  public Model(final Member owner, final $Member binding, final String name) {
    super(owner, binding, name);
  }

  public Model(final Member owner, final String doc) {
    super(owner, (String)null, doc);
  }

  public Model(final Element copy) {
    super(copy);
  }

  protected String reference() {
    return name();
  }

  protected abstract Model merge(final Template reference);
}