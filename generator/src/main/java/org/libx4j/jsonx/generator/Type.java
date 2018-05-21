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

package org.libx4j.jsonx.generator;

import java.util.HashMap;

class Type {
  private static final HashMap<String,Type> qualifiedNameToType = new HashMap<String,Type>();
  static final Type OBJECT = new Type(Object.class, null);

  public static Type get(final String packageName, final String compoundName, final String superCompoundName, final Type genericType) {
    if (compoundName == null)
      throw new NullPointerException("compoundName == null");

    final String className = packageName != null ? packageName + "." + compoundName : compoundName;
    Type type = qualifiedNameToType.get(className);
    if (type == null)
      qualifiedNameToType.put(className, type = new Type(packageName, compoundName, superCompoundName == null ? null : get(packageName, superCompoundName, (Class<?>)null, null), genericType));

    return type;
  }

  public static Type get(final String className, final Type superType, final Type genericType) {
    return get(null, className, superType.name, genericType);
  }

  public static Type get(final String className, final String superName, final Type genericType) {
    return get(null, className, superName, genericType);
  }

  public static Type get(final String packageName, final String compoundName, final Class<?> superType, final Type genericType) {
    return get(packageName, compoundName, superType == null ? null : superType.getName(), genericType);
  }

  private static Type get(final Class<?> clazz) {
    if (clazz == null)
      return null;

    Type type = qualifiedNameToType.get(clazz.getName());
    if (type == null)
      qualifiedNameToType.put(clazz.getName(), type = new Type(clazz, null));

    return type;
  }

  public static Type get(final Class<?> clazz, final Type generic) {
    final String name = clazz.getName() + " " + generic.toString();
    Type type = qualifiedNameToType.get(name);
    if (type == null)
      qualifiedNameToType.put(name, type = new Type(clazz, generic));

    return type;
  }

  public static Type get(final Class<?> ... generics) {
    return get(0, generics);
  }

  private static Type get(final int index, final Class<?> ... generics) {
    return index == generics.length - 1 ? get(generics[index]) : get(generics[index], get(index + 1, generics));
  }

  public static Type get(final String compoundName, final String superName) {
    return get(null, compoundName, superName, (Type)null);
  }

  public static String getSubName(final String name, final String pacakgeName) {
    return pacakgeName != null && name.startsWith(pacakgeName) ? name.substring(pacakgeName.length() + 1) : name;
  }

  private final String packageName;
  private final String strictPackageName;
  private final String simpleName;
  private final String compoundName;
  private final String strictCompoundClassName;
  private final String name;
  private final String strictName;
  private final Type superType;
  private final Type genericType;

  private Type(final String packageName, final String compoundName, final Type superType, final Type genericType) {
    this.packageName = packageName;
    final int dot = compoundName.lastIndexOf('.');
    this.strictPackageName = dot == -1 ? packageName : packageName != null ? packageName + "." + compoundName.substring(0, dot) : compoundName.substring(0, dot);
    this.compoundName = compoundName;
    this.name = packageName != null ? packageName + "." + compoundName : compoundName;
    if (name.endsWith("AbstractObject")) {
      int i = 0;
    }

    this.strictCompoundClassName = compoundName.replace('$', '.');
    this.simpleName = strictCompoundClassName.substring(strictCompoundClassName.lastIndexOf('.') + 1);
    this.strictName = packageName + "." + strictCompoundClassName;
    qualifiedNameToType.put(name, this);
    this.superType = superType != null ? superType : OBJECT;
    this.genericType = genericType;
  }

  private Type(final Class<?> clazz, final Type genericType) {
    this(clazz.getPackage().getName(), clazz.getSimpleName(), clazz.getSuperclass() == null ? null : clazz.getSuperclass() == Object.class ? OBJECT : new Type(clazz.getSuperclass(), null), genericType);
  }

  public Type getSuperType() {
    return superType;
  }

  public Type getDeclaringType() {
    final int del = compoundName.lastIndexOf('$');
    return del < 0 ? null : get(packageName, compoundName.substring(0, del), (Class<?>)null, null);
  }

  public Type getGreatestCommonSuperType(final Type type) {
    Type a = this;
    do {
      Type b = type;
      do {
        if (a.name.equals(b.name))
          return a;

        b = b.superType;
      }
      while (b != null);
      a = a.superType;
    }
    while (a != null);
    return OBJECT;
  }

  public String getPackage() {
    return packageName;
  }

  public String getStrictPackage() {
    return strictPackageName;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getCompoundClassName() {
    return compoundName;
  }

  public String getStrictCompoundClassName() {
    return strictCompoundClassName;
  }

  public String getName(final String pacakgeName) {
    final StringBuilder builder = new StringBuilder();
    if (this.packageName != null)
      builder.append(this.packageName).append('.');
    else if (packageName != null)
      builder.append(pacakgeName).append('.');

    builder.append(compoundName);
    return builder.toString();
  }

  public String getStrictName() {
    return strictName;
  }

  public String getSubName(final String pacakgeName) {
    return getSubName(name, pacakgeName);
  }

  public String toStrictString() {
    final StringBuilder builder = new StringBuilder(getStrictName());
    if (genericType != null)
      builder.append('<').append(genericType.toStrictString()).append('>');

    return builder.toString();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder(name);
    if (genericType != null)
      builder.append('<').append(genericType.toString()).append('>');

    return builder.toString();
  }
}