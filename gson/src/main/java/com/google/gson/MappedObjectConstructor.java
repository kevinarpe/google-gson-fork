/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import sun.misc.Unsafe;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains a mapping of all the application specific
 * {@link InstanceCreator} instances.  Registering an {@link InstanceCreator}
 * with this class will override the default object creation that is defined
 * by the ObjectConstructor that this class is wrapping.  Using this class
 * with the JSON framework provides the application with "pluggable" modules
 * to customize framework to suit the application's needs.
 *
 * @author Joel Leitch
 */
final class MappedObjectConstructor implements ObjectConstructor {
  private static final Logger log = Logger.getLogger(MappedObjectConstructor.class.getName());
  private static final Unsafe THE_UNSAFE = getUnsafe();

  private final ParameterizedTypeHandlerMap<InstanceCreator<?>> instanceCreatorMap;
  
  public MappedObjectConstructor(
      ParameterizedTypeHandlerMap<InstanceCreator<?>> instanceCreators) {
    instanceCreatorMap = instanceCreators;
  }

  @SuppressWarnings("unchecked")
  public <T> T construct(Type typeOfT) {
    InstanceCreator<T> creator = (InstanceCreator<T>) instanceCreatorMap.getHandlerFor(typeOfT);
    if (creator != null) {
      return creator.createInstance(typeOfT);
    }
    return (T) constructWithNoArgConstructor(typeOfT);
  }
  
  private static Unsafe getUnsafe() {
    try {
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      return (Unsafe) f.get(null);
    } catch (NoSuchFieldException e) {
      throw new Error();
    } catch (IllegalAccessException e) {
      throw new Error();
    }
  }

  public Object constructArray(Type type, int length) {
    return Array.newInstance(Types.getRawType(type), length);
  }

  @SuppressWarnings({"unchecked", "cast"})
  private <T> T constructWithNoArgConstructor(Type typeOfT) {
    try {
      Class<T> clazz = (Class<T>) Types.getRawType(typeOfT);
      Constructor<T> constructor = getNoArgsConstructor(clazz);
      if (constructor == null) {
        return (T) THE_UNSAFE.allocateInstance(clazz);
      }
      return constructor.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(("Unable to invoke no-args constructor for " + typeOfT + ". "
          + "Register an InstanceCreator with Gson for this type may fix this problem."), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(("Unable to invoke no-args constructor for " + typeOfT + ". "
          + "Register an InstanceCreator with Gson for this type may fix this problem."), e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(("Unable to invoke no-args constructor for " + typeOfT + ". "
          + "Register an InstanceCreator with Gson for this type may fix this problem."), e);
    }
  }

  private <T> Constructor<T> getNoArgsConstructor(Class<T> clazz) {
    try {
      Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
      declaredConstructor.setAccessible(true);
      return declaredConstructor;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Use this methods to register an {@link InstanceCreator} for a new type.
   *
   * @param <T> the type of class to be mapped with its "creator"
   * @param typeOfT the instance type that will be created
   * @param creator the {@link InstanceCreator} instance to register
   */
  <T> void register(Type typeOfT, InstanceCreator<? extends T> creator) {
    if (instanceCreatorMap.hasSpecificHandlerFor(typeOfT)) {
      log.log(Level.WARNING, "Overriding the existing InstanceCreator for {0}", typeOfT);
    }
    instanceCreatorMap.register(typeOfT, creator);
  }
  
  @Override
  public String toString() {
    return instanceCreatorMap.toString();
  }
}
