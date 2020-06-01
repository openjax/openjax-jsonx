/* Copyright (c) 2017 JSONx
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

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.jsonx.www.schema_0_4.xL0gluGCXAA.$Documented;
import org.jsonx.www.schema_0_4.xL0gluGCXAA.$FieldIdentifier;
import org.jsonx.www.schema_0_4.xL0gluGCXAA.$MaxOccurs;
import org.libj.lang.Classes;
import org.libj.util.CollectionUtil;
import org.openjax.xml.api.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.www._2001.XMLSchema.yAA.$AnySimpleType;
import org.w3.www._2001.XMLSchema.yAA.$Boolean;
import org.w3.www._2001.XMLSchema.yAA.$NonNegativeInteger;
import org.w3.www._2001.XMLSchema.yAA.$String;

import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;

abstract class Model extends Member implements Comparable<Model> {
  private static final Logger logger = LoggerFactory.getLogger(Model.class);

  static Class<?> toClass(final Type type) {
    if (type instanceof Class)
      return (Class<?>)type;

    if (type instanceof WildcardTypeImpl)
      return (Class<?>)((WildcardTypeImpl)type).getUpperBounds()[0];

    return null;
  }

  static boolean isAssignable(final Method getMethod, final boolean canWrap, final Class<?> cls, final boolean isRegex, final boolean nullable, final Use use) {
    final Class<?> type = canWrap && getMethod.getReturnType().isPrimitive() ? Classes.toWrapper(getMethod.getReturnType()) : getMethod.getReturnType();
    if (isRegex) {
      if (!Map.class.isAssignableFrom(type))
        return false;

      final Type genericType = getMethod.getGenericReturnType();
      if (!(genericType instanceof ParameterizedType))
        return false;

      final Type[] genericTypes = ((ParameterizedType)genericType).getActualTypeArguments();
      if (genericTypes.length != 2)
        return false;

      if (!(genericTypes[1] instanceof ParameterizedType))
        return cls == null || toClass(genericTypes[1]).isAssignableFrom(cls);

      final Class<?> valueType = (Class<?>)((ParameterizedType)genericTypes[1]).getRawType();
      if (valueType != Optional.class)
        return false;

      final Type[] args = ((ParameterizedType)genericTypes[1]).getActualTypeArguments();
      return args.length == 1 && (cls == null || cls.isAssignableFrom(args[0] instanceof ParameterizedType ? (Class<?>)((ParameterizedType)args[0]).getRawType() : toClass(args[0])));
    }

    if (use != Use.OPTIONAL || !nullable)
      return cls == null || cls.isAssignableFrom(type);

    if (type != Optional.class)
      return false;

    final Class<?>[] genericTypes = Classes.getGenericParameters(getMethod);
    if (genericTypes.length == 0 || genericTypes[0] == null)
      return false;

    return cls == null || cls.isAssignableFrom(genericTypes[0]);
  }

  final void validateTypeBinding() {
    if (typeBinding == null || typeBinding.type == null)
      return;

    if (typeBinding.type.isPrimitive() && !typeBinding.type.isArray()) {
      if (use.set == Use.OPTIONAL)
        throw new ValidationException("\"" + fullyQualifiedDisplayName(declarer) + "\" cannot declare \"" + displayName() + "\" with primitive type \"" + typeBinding.type.getCompoundName() + "\" with use=optional");

      if (nullable == null && !(declarer instanceof SchemaElement))
        throw new ValidationException("\"" + fullyQualifiedDisplayName(declarer) + "\" cannot declare \"" + displayName() + "\" with primitive type \"" + typeBinding.type.getCompoundName() + "\" with nullable=true");
    }

    // Check that we have: ? super CharSequence -> decode -> [type] -> encode -> ? extends CharSequence
    final Class<?> cls = Classes.forNameOrNull(typeBinding.type.getNativeName(), false, getClass().getClassLoader());
    Executable decodeMethod = null;
    boolean preventDefault = false;
    if (typeBinding.decode != null) {
      try {
        decodeMethod = JsdUtil.parseExecutable(typeBinding.decode, String.class);
      }
      catch (final ValidationException e) {
        preventDefault = true;
        if (e.getCause() instanceof ClassNotFoundException)
          logger.warn("Unable to validate \"decode\": " + typeBinding.decode + " due to: " + e.getCause().getMessage());
        else
          throw e;
      }
    }

    Executable encodeMethod = null;
    if (cls != null && typeBinding.encode != null) {
      try {
        encodeMethod = JsdUtil.parseExecutable(typeBinding.encode, cls);
      }
      catch (final ValidationException e) {
        preventDefault = true;
        if (e.getCause() instanceof ClassNotFoundException)
          logger.warn("Unable to validate \"encode\": " + typeBinding.encode + " due to: " + e.getCause().getMessage());
        else
          throw e;
      }
    }

    if (preventDefault)
      return;

    String error = null;

    if (cls != null) {
      if (decodeMethod != null) {
        if (!Classes.isAssignableFrom(cls, JsdUtil.getReturnType(decodeMethod))) {
          error = "The return type of \"decode\" method \"" + decodeMethod + "\" in " + JsdUtil.flipName(id().toString()) + " is not assignable to: " + typeBinding.type.getName();
        }
      }
      else if (encodeMethod == null) {
        if (!Classes.isAssignableFrom(defaultClass(), cls) && !Classes.isAssignableFrom(CharSequence.class, cls)) {
          error = "The type binding \"" + typeBinding.type.getName() + "\" in " + JsdUtil.flipName(id().toString()) + " is not \"encode\" compatible with " + defaultClass().getName() + " or " + CharSequence.class.getName();
        }
      }
    }
    else if (encodeMethod != null && !Classes.isAssignableFrom(CharSequence.class, JsdUtil.getReturnType(encodeMethod))) {
      error = "The return type of \"encode\" method \"" + encodeMethod + "\" in " + JsdUtil.flipName(id().toString()) + " is not assignable to:" + CharSequence.class.getName();
    }
    else if (decodeMethod != null && !Classes.isAssignableFrom(defaultClass(), JsdUtil.getReturnType(decodeMethod))) {
      error = "The return type of \"decode\" method \"" + decodeMethod + "\" in " + JsdUtil.flipName(id().toString()) + " is not assignable to: " + defaultClass().getName();
    }

    if (error != null)
      throw new ValidationException(error);

    error = isValid(typeBinding);
    if (error != null)
      throw new ValidationException(error);
  }

  Model(final Registry registry, final Declarer declarer, final Id id, final $Documented.Doc$ doc, final $AnySimpleType name, final $Boolean nullable, final $String use, final $FieldIdentifier fieldName, final Binding.Type typeBinding) {
    super(registry, declarer, true, id, doc, name, nullable, use, fieldName, typeBinding);
  }

  Model(final Registry registry, final Declarer declarer, final Id id, final $Documented.Doc$ doc, final $Boolean nullable, final $NonNegativeInteger minOccurs, final $MaxOccurs maxOccurs, final Binding.Type typeBinding) {
    super(registry, declarer, true, id, doc, nullable, minOccurs, maxOccurs, typeBinding);
  }

  Model(final Registry registry, final Declarer declarer, final Id id, final $Documented.Doc$ doc, final Binding.Type typeBinding) {
    super(registry, declarer, true, id, doc, null, null, null, null, null, null, typeBinding);
  }

  Model(final Registry registry, final Declarer declarer, final Id id, final Boolean nullable, final Use use, final String fieldName, final Binding.Type typeBinding) {
    super(registry, declarer, false, id, null, null, nullable, use, null, null, fieldName, typeBinding);
  }

  String sortKey() {
    return getClass().getSimpleName() + id();
  }

  @Override
  public final int compareTo(final Model o) {
    return sortKey().compareTo(o.sortKey());
  }

  @Override
  Map<String,Object> toXmlAttributes(final Element owner, final String packageName) {
    final Map<String,Object> attributes = super.toXmlAttributes(owner, packageName);
    if (owner instanceof ObjectModel)
      attributes.put("xsi:type", elementName());

    return attributes;
  }

  @Override
  XmlElement toXml(final Settings settings, final Element owner, final String packageName) {
    final Map<String,Object> attributes = toXmlAttributes(owner, packageName);
    final XmlElement element = new XmlElement(owner instanceof ObjectModel ? "property" : elementName(), attributes, null);
    final Map<String,Object> bindingAttributes = Binding.toXmlAttributes(owner, typeBinding, fieldBinding);
    if (bindingAttributes != null)
      element.setElements(CollectionUtil.asCollection(new ArrayList<>(), new XmlElement("binding", bindingAttributes)));

    return element;
  }

  @Override
  Map<String,Object> toJson(final Settings settings, final Element owner, final String packageName) {
    final Map<String,Object> properties = new LinkedHashMap<>();
    properties.put("jx:type", elementName());

    final Map<String,Object> attributes = toXmlAttributes(owner, packageName);
    attributes.remove(nameName());
    attributes.remove("xsi:type");

    properties.putAll(attributes);
    final Map<String,Object> bindingAttributes = Binding.toXmlAttributes(owner, typeBinding, fieldBinding);
    if (bindingAttributes != null)
      properties.put("bindings", Collections.singletonList(bindingAttributes));

    return properties;
  }

  abstract String isValid(Binding.Type typeBinding);
}