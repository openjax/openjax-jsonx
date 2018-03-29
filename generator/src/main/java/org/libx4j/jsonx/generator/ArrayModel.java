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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lib4j.lang.AnnotationParameterException;
import org.lib4j.lang.IllegalAnnotationException;
import org.lib4j.util.Iterators;
import org.libx4j.jsonx.jsonx_0_9_7.xL2gluGCXYYJc.$Array;
import org.libx4j.jsonx.jsonx_0_9_7.xL2gluGCXYYJc.$Element;
import org.libx4j.jsonx.jsonx_0_9_7.xL2gluGCXYYJc.$Object;
import org.libx4j.jsonx.jsonx_0_9_7.xL2gluGCXYYJc.Jsonx;
import org.libx4j.jsonx.runtime.ArrayElement;
import org.libx4j.jsonx.runtime.ArrayProperty;
import org.libx4j.jsonx.runtime.BooleanElement;
import org.libx4j.jsonx.runtime.NumberElement;
import org.libx4j.jsonx.runtime.ObjectElement;
import org.libx4j.jsonx.runtime.StringElement;
import org.libx4j.xsb.runtime.Bindings;

class ArrayModel extends ComplexModel {
  private static StringBuilder writeElementIdsClause(final StringBuilder builder, final int numElements, final int offset) {
    builder.append("elementIds=");
    if (numElements == 0)
      return builder.append("{}");

    if (numElements == 1)
      return builder.append(offset);

    builder.append('{');
    for (int i = 0; i < numElements - 1; i++)
      builder.append(i + offset).append(", ");

    return builder.append(numElements - 1 + offset).append('}');
  }

  protected static List<Object> getGreatestCommonSuperObject(final List<Element> elements) {
    if (elements.size() == 1)
      return Collections.singletonList(elements.get(0).className());

    List<Object> gcc = getGreatestCommonSuperObject(elements.get(0), elements.get(1));
    for (int i = 2; i < elements.size() && gcc != null; i++) {
      if (gcc instanceof Element)
        gcc = getGreatestCommonSuperObject((Element)gcc, elements.get(i));
      else if (Number.class.toString().equals(gcc) && !(elements.get(i) instanceof NumberModel))
        gcc = Collections.singletonList(Object.class.getName());
      else if (Object.class.getName().equals(gcc))
        return gcc;
    }

    return gcc;
  }

  private static List<Object> getGreatestCommonSuperObject(final Element a, final Element b) {
    if (a.className().equals(b.className()))
      return Collections.singletonList(a);

    if (a instanceof ObjectModel)
      return Collections.singletonList(b instanceof ObjectModel ? getGreatestCommonSuperObject((ObjectModel)a, (ObjectModel)b) : Object.class.getName());

    if (b instanceof ObjectModel)
      return Collections.singletonList(Object.class.getName());

    if (a instanceof NumberModel && b instanceof NumberModel)
      return Collections.singletonList(Number.class.toString());

    if (a instanceof ArrayModel && b instanceof ArrayModel) {
      final List<Element> all = new ArrayList<Element>(((ArrayModel)a).members());
      all.addAll(((ArrayModel)b).members());
      final List<Object> gcc = getGreatestCommonSuperObject(all);
      gcc.set(0, List.class.getName());
      return gcc;
    }

    if (a instanceof ArrayModel || b instanceof ArrayModel)
      return Collections.singletonList(Object.class.getName());

    return Collections.singletonList(Object.class.getName());
  }

  private static Object getGreatestCommonSuperObject(ObjectModel a, ObjectModel b) {
    do {
      do
        if (a.className().equals(b.className()))
          return a;
      while ((b = b.superObject()) != null);
    }
    while ((a = a.superObject()) != null);
    return Object.class.getName();
  }

  private static List<Element> parseMembers(final Schema schema, final Registry registry, final ArrayModel referrer, final $Array binding) {
    final List<Element> members = new LinkedList<Element>();
    final Iterator<? extends $Element> elements = Iterators.filter(binding.elementIterator(), $Element.class);
    while (elements.hasNext()) {
      final $Element element = elements.next();
      if (element instanceof $Array.Boolean) {
        members.add(BooleanModel.reference(schema, registry, referrer, ($Array.Boolean)element));
      }
      else if (element instanceof $Array.Number) {
        members.add(NumberModel.reference(schema, registry, referrer, ($Array.Number)element));
      }
      else if (element instanceof $Array.String) {
        members.add(StringModel.reference(schema, registry, referrer, ($Array.String)element));
      }
      else if (element instanceof $Array.Array) {
        final $Array.Array member = ($Array.Array)element;
        final ArrayModel child = ArrayModel.reference(schema, registry, referrer, member);
        members.add(child);
      }
      else if (element instanceof $Array.Object) {
        final $Array.Object member = ($Array.Object)element;
        final ObjectModel child = ObjectModel.reference(schema, registry, referrer, member, member.getExtends$() == null ? null : member.getExtends$().text());
        members.add(child);
      }
      else if (element instanceof $Array.Ref) {
        final $Array.Ref member = ($Array.Ref)element;
        final Element ref = registry.getElement(member.getProperty$().text());
        if (ref == null)
          throw new IllegalStateException("Top-level element ref=\"" + member.getProperty$().text() + "\" in array not found");

        members.add(ref instanceof Model ? new RefElement(schema, member, (Model)ref) : ref);
      }
      else {
        throw new UnsupportedOperationException("Unsupported " + element.getClass().getSimpleName() + " member type: " + element.getClass().getName());
      }
    }

    return Collections.unmodifiableList(members);
  }

  private static List<Element> parseMembers(final Schema schema, final Registry registry, final ArrayModel referrer, final Field field, final Annotation annotation, final int[] elementIds, final Map<Integer,Annotation> annotations) {
    final List<Element> elements = new ArrayList<Element>();
    for (final Integer elementId : elementIds) {
      final Annotation elementAnnotation = annotations.get(elementId);
      if (elementAnnotation == null)
        throw new AnnotationParameterException(annotation, field.getDeclaringClass().getName() + "." + field.getName() + ": @" + annotation.annotationType().getName() + " specifies non-existent element with id=" + elementId);

      if (elementAnnotation instanceof BooleanElement)
        elements.add(BooleanModel.referenceOrDeclare(schema, registry, referrer, (BooleanElement)elementAnnotation));
      else if (elementAnnotation instanceof NumberElement)
        elements.add(NumberModel.referenceOrDeclare(schema, registry, referrer, (NumberElement)elementAnnotation));
      else if (elementAnnotation instanceof StringElement)
        elements.add(StringModel.referenceOrDeclare(schema, registry, referrer, (StringElement)elementAnnotation));
      else if (elementAnnotation instanceof ObjectElement)
        elements.add(ObjectModel.referenceOrDeclare(schema, registry, referrer, (ObjectElement)elementAnnotation));
      else if (elementAnnotation instanceof ArrayElement)
        elements.add(ArrayModel.referenceOrDeclare(schema, registry, referrer, (ArrayElement)elementAnnotation, field, annotations));
    }

    return Collections.unmodifiableList(elements);
  }

  public static ArrayModel reference(final Schema schema, final Registry registry, final ComplexModel referrer, final $Array.Array binding) {
    return new ArrayModel(schema, registry, binding);
  }

  // Annullable, Recurrable
  private ArrayModel(final Schema schema, final Registry registry, final $Array.Array binding) {
    super(schema, binding, binding.getNullable$().text(), binding.getMinOccurs$(), binding.getMaxOccurs$());
    if (this.maxOccurs() != null && this.minOccurs() > this.maxOccurs())
      throw new ValidationException(Bindings.getXPath(binding, elementXPath) + ": minOccurs=\"" + this.minOccurs() + "\" > maxOccurs=\"" + this.maxOccurs() + "\"");

    this.members = parseMembers(schema, registry, this, binding);
  }

  public static ArrayModel reference(final Schema schema, final Registry registry, final ComplexModel referrer, final $Object.Array binding) {
    return new ArrayModel(schema, registry, binding);
  }

  // Nameable, Annullable, Requirable
  private ArrayModel(final Schema schema, final Registry registry, final $Object.Array binding) {
    super(schema, binding, binding.getName$().text(), binding.getRequired$().text(), binding.getNullable$().text());
    this.members = parseMembers(schema, registry, this, binding);
  }

  // Nameable
  public static ArrayModel declare(final Schema schema, final Registry registry, final Jsonx.Array binding) {
    return registry.declare(binding).value(new ArrayModel(schema, registry, binding, binding.getDoc$() == null ? null : binding.getDoc$().text()), null);
  }

  private ArrayModel(final Schema schema, final Registry registry, final Jsonx.Array binding, final String doc) {
    super(schema, binding.getName$().text(), null, null, null, null, doc);
    this.members = parseMembers(schema, registry, this, binding);
  }

  public static ArrayModel referenceOrDeclare(final Schema schema, final Registry registry, final ComplexModel referrer, final ArrayProperty arrayProperty, final Field field) {
    return new ArrayModel(schema, registry, arrayProperty, field);
  }

  private static final Function<Annotation,Integer> reference = new Function<Annotation,Integer>() {
    @Override
    public Integer apply(final Annotation t) {
      if (t instanceof BooleanElement)
        return ((BooleanElement)t).id();

      if (t instanceof NumberElement)
        return ((NumberElement)t).id();

      if (t instanceof StringElement)
        return ((StringElement)t).id();

      if (t instanceof ArrayElement)
        return ((ArrayElement)t).id();

      if (t instanceof ObjectElement)
        return ((ObjectElement)t).id();

      throw new UnsupportedOperationException("Unsupported Annotation type: " + t.getClass());
    }
  };

  private static void addElementAnnotation(final Map<Integer,Annotation> map, final StrictDigraph<Integer> digraph, final Annotation annotation, final int id, final Field field) {
    if (map.containsKey(id))
      throw new AnnotationParameterException(annotation, field.getDeclaringClass().getName() + "." + field.getName() + ": @" + BooleanElement.class.getName() + "(id=" + id + ") cannot share the same id value with another @*Element(id=?) annotation on the same field.");

    digraph.addVertex(reference.apply(annotation));
    map.put(id, annotation);
  }

  private ArrayModel(final Schema schema, final Registry registry, final ArrayProperty arrayProperty, final Field field) {
    // FIXME: Can we get doc comments from code?
    super(schema, getName(arrayProperty.name(), field), arrayProperty.required(), arrayProperty.nullable(), null, null, null);
    if (field.getType() != List.class && !field.getType().isArray())
      throw new IllegalAnnotationException(arrayProperty, field.getDeclaringClass().getName() + "." + field.getName() + ": @" + ArrayProperty.class.getSimpleName() + " can only be applied to fields of array or List types.");

    final Map<Integer,Annotation> map = new HashMap<Integer,Annotation>();
    final StrictDigraph<Integer> digraph = new StrictDigraph<Integer>("Element cannot include itself as a member");
    final BooleanElement[] booleanElements = field.getAnnotationsByType(BooleanElement.class);
    if (booleanElements != null)
      for (final BooleanElement booleanElement : booleanElements)
        addElementAnnotation(map, digraph, booleanElement, booleanElement.id(), field);

    final NumberElement[] numberElements = field.getAnnotationsByType(NumberElement.class);
    if (numberElements != null)
      for (final NumberElement numberElement : numberElements)
        addElementAnnotation(map, digraph, numberElement, numberElement.id(), field);

    final StringElement[] stringElements = field.getAnnotationsByType(StringElement.class);
    if (stringElements != null)
      for (final StringElement stringElement : stringElements)
        addElementAnnotation(map, digraph, stringElement, stringElement.id(), field);

    final ObjectElement[] objectElements = field.getAnnotationsByType(ObjectElement.class);
    if (objectElements != null)
      for (final ObjectElement objectElement : objectElements)
        addElementAnnotation(map, digraph, objectElement, objectElement.id(), field);

    final ArrayElement[] arrayElements = field.getAnnotationsByType(ArrayElement.class);
    if (arrayElements != null) {
      for (final ArrayElement arrayElement : arrayElements) {
        addElementAnnotation(map, digraph, arrayElement, arrayElement.id(), field);
        for (final Integer arrayElementId : arrayElement.elementIds())
          digraph.addEdge(arrayElement.id(), arrayElementId);
      }
    }

    final List<Integer> cycle = digraph.getCycle();
    if (cycle != null)
      throw new ValidationException("Cycle detected in element index dependency graph: " + org.lib4j.util.Collections.toString(digraph.getCycle(), " -> "));

    final LinkedHashMap<Integer,Annotation> topologicalOrder = new LinkedHashMap<Integer,Annotation>(map.size());
    for (final Integer elementId : digraph.getTopologicalOrder())
      topologicalOrder.put(elementId, map.get(elementId));

    this.members = parseMembers(schema, registry, this, field, arrayProperty, arrayProperty.elementIds(), topologicalOrder);
  }

  private final List<Element> members;

  private static ArrayModel referenceOrDeclare(final Schema schema, final Registry registry, final ComplexModel referrer, final ArrayElement arrayElement, final Field field, final Map<Integer,Annotation> annotations) {
    return new ArrayModel(schema, registry, arrayElement, field, annotations);
  }

  private ArrayModel(final Schema schema, final Registry registry, final ArrayElement arrayElement, final Field field, final Map<Integer,Annotation> annotations) {
    // FIXME: Can we get doc comments from code?
    super(schema, null, null, arrayElement.nullable(), arrayElement.minOccurs(), arrayElement.maxOccurs(), null);
    this.members = parseMembers(schema, registry, this, field, arrayElement, arrayElement.elementIds(), annotations);
  }

  private ArrayModel(final Element element, final List<Element> members) {
    super(element);
    this.members = Collections.unmodifiableList(members);
  }

  public final List<Element> members() {
    return this.members;
  }

  @Override
  protected final Type className() {
    final List<Object> cls = getGreatestCommonSuperObject(members);
    final StringBuilder builder = new StringBuilder(List.class.getName());
    for (final Object part : cls)
      builder.append('<').append(part instanceof Element ? ((Element)part).className() : part);

    for (int i = 0; i < cls.size(); i++)
      builder.append('>');

    // FIXME: Finish this...
    return null;
  }

  @Override
  protected final Class<? extends Annotation> propertyAnnotation() {
    return ArrayProperty.class;
  }

  @Override
  protected final Class<? extends Annotation> elementAnnotation() {
    return ArrayElement.class;
  }

  @Override
  protected final ArrayModel normalize(final Registry registry) {
    return new ArrayModel(this, normalize(registry, members));
  }

  @Override
  protected ArrayModel merge(final RefElement propertyElement) {
    return new ArrayModel(propertyElement, members);
  }

  @Override
  protected final void collectClassNames(final List<Type> types) {
    if (members != null)
      for (final Element member : members)
        member.collectClassNames(types);
  }

  @Override
  protected final String toJSON(final String pacakgeName) {
    final StringBuilder builder = new StringBuilder(super.toJSON(pacakgeName));
    if (builder.length() > 0)
      builder.insert(0, ",\n");

    if (members != null) {
      builder.append(",\n  members: ");
      final StringBuilder members = new StringBuilder();
      for (final Element member : this.members)
        members.append(", ").append(member.toJSON(pacakgeName).replace("\n", "\n  "));

      builder.append('[').append(members.length() > 0 ? members.substring(2) : members.toString()).append(']');
    }

    return "{\n" + (builder.length() > 0 ? builder.substring(2) : builder.toString()) + "\n}";
  }

  @Override
  protected final String toJSONX(final String pacakgeName) {
    final StringBuilder builder = new StringBuilder("<array");
    if (members.size() > 0) {
      builder.append(super.toJSONX(pacakgeName)).append('>');
      for (final Element member : this.members)
        builder.append("\n  ").append(member.toJSONX(pacakgeName).replace("\n", "\n  "));

      builder.append("\n</array>");
    }
    else {
      builder.append(super.toJSONX(pacakgeName)).append("/>");
    }

    return builder.toString();
  }

  @Override
  protected final String toAnnotation(final boolean full) {
    final StringBuilder builder = full ? new StringBuilder(super.toAnnotation(full)) : new StringBuilder();
    if (builder.length() > 0)
      builder.append(", ");

    return writeElementIdsClause(builder, members.size(), 0).toString();
  }

  protected final StringBuilder toAnnotation(final StringBuilder builder, final int numElements, final int offset) {
    builder.append(super.toAnnotation(true));
    if (builder.length() > 0)
      builder.append(", ");

    return writeElementIdsClause(builder, members.size(), offset);
  }

  private static int writeElementAnnotations(final StringBuilder builder, final Element element, final int index) {
    final StringBuilder params = new StringBuilder("@").append(element.elementAnnotation().getName()).append("(id=").append(index);
    if (element instanceof ArrayModel) {
      final ArrayModel arrayModel = (ArrayModel)element;
      int diff = arrayModel.members().size();
      builder.insert(0, arrayModel.toAnnotation(params, arrayModel.members().size(), index + arrayModel.members().size()).append(")\n"));
      for (int i = 0; i < arrayModel.members().size(); i++) {
        final Element member = arrayModel.members().get(i);
        diff += writeElementAnnotations(builder, member, i + arrayModel.members().size());
      }

      return diff;
    }

    final String annotation = element.toAnnotation(true);
    builder.insert(0, (annotation.length() > 0 ? params.append(", ").append(annotation) : params).append(")\n"));
    return 1;
  }

  @Override
  protected final String toField() {
    final StringBuilder builder = new StringBuilder();
    int diff = 0;
    for (int i = 0; i < members.size(); i++)
      diff += writeElementAnnotations(builder, members.get(i), diff);

    return builder.append(super.toField()).toString();
  }
}