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

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.reflect.DisjunctionExclusionStrategy;
import com.google.gson.reflect.ExclusionStrategy;
import com.google.gson.reflect.InnerClassExclusionStrategy;
import com.google.gson.reflect.ModifierBasedExclusionStrategy;
import com.google.gson.reflect.ObjectNavigatorFactory;
import com.google.gson.version.VersionConstants;
import com.google.gson.version.VersionExclusionStrategy;

/**
 * Use this builder to construct a Gson instance in situations where
 * you need to set a number of parameters. 
 * 
 * @author Inderjeet Singh
 */
public final class GsonBuilder {
  
  private double ignoreVersionsAfter;
  private ModifierBasedExclusionStrategy modifierBasedExclusionStrategy;
  private InnerClassExclusionStrategy innerClassExclusionStrategy;
  private TypeAdapter typeAdapter;
  private JsonFormatter formatter;
  private final Map<Type, InstanceCreator<?>> instanceCreators;
  private final Map<Type, JsonSerializer<?>> serializers;
  private final Map<Type, JsonDeserializer<?>> deserializers;
  
  public GsonBuilder() {
    // setup default values    
    ignoreVersionsAfter = VersionConstants.IGNORE_VERSIONS;    
    innerClassExclusionStrategy = new InnerClassExclusionStrategy();    
    modifierBasedExclusionStrategy = Gson.DEFAULT_MODIFIER_BASED_EXCLUSION_STRATEGY;    
    typeAdapter = Gson.DEFAULT_TYPE_ADAPTER;
    formatter = Gson.DEFAULT_JSON_FORMATTER;
    instanceCreators = new LinkedHashMap<Type, InstanceCreator<?>>();
    serializers = new LinkedHashMap<Type, JsonSerializer<?>>();
    deserializers = new LinkedHashMap<Type, JsonDeserializer<?>>();
  }
  
  /**
   * Use this setter to enable versioning support. 
   * @param ignoreVersionsAfter any field or type marked with a
   *        version higher than this value are ignored during serialization
   *        or deserialization.
   */
  public GsonBuilder setVersion(double ignoreVersionsAfter) {
    this.ignoreVersionsAfter = ignoreVersionsAfter;
    return this;
  }
  
  /**
   * Setup Gson such that it excludes all class fields that have
   * the specified modifiers. By default, Gson will exclude all fields
   * marked transient or static. This method will override that behavior. 
   *  
   * @param modifiers the field modifiers. You must use the modifiers
   *        specified in the {@link java.lang.reflect.Modifier} class. For example, 
   *        {@link java.lang.reflect.Modifier#TRANSIENT}, 
   *        {@link java.lang.reflect.Modifier#STATIC}
   */
  public GsonBuilder excludeFieldsWithModifiers(int... modifiers) {
    boolean skipSynthetics = true;
    modifierBasedExclusionStrategy = new ModifierBasedExclusionStrategy(skipSynthetics, modifiers);
    return this;    
  }
  
  /**
   * Setup Gson with a new formatting strategy other than the default strategu 
   * which is to provide a compact representation that eliminates all unneeded 
   * white-space.
   *  
   * @param formatter the new formatter to use
   * @see JsonPrintFormatter
   */
  public GsonBuilder setFormatter(JsonFormatter formatter) {
    this.formatter = formatter;
    return this;
  }
  
  /**
   * Registers an instance creator for the specified class. If an
   * instance creator was previously registered for the specified
   * class, it is overwritten. You should use this method if you
   * want to register a single instance creator for all generic
   * types mapping to a single raw type. If you want different
   * handling for different generic types of a single raw type,
   * use {@link #registerInstanceCreator(Type, InstanceCreator)}
   * instead.
   *
   * @param <T> the type for which instance creator is being registered
   * @param classOfT The class definition for the type T
   * @param instanceCreator the instance creator for T
   */
  public <T> GsonBuilder registerInstanceCreator(Class<T> classOfT,
      InstanceCreator<? extends T> instanceCreator) {
    return registerInstanceCreator((Type) classOfT, instanceCreator);
  }

  /**
   * Registers an instance creator for the specified type. If an
   * instance creator was previously registered for the specified
   * class, it is overwritten. Since this method takes a type instead
   * of a Class object, it can be used to register a specific handler
   * for a generic type corresponding to a raw type. If you want to
   * have common handling for all generic types corresponding to a
   * raw type, use
   * {@link #registerInstanceCreator(Class, InstanceCreator)} instead.
   *
   * @param <T> the type for which instance creator is being registered
   * @param typeOfT The Type definition for T
   * @param instanceCreator the instance creator for T
   */
  public <T> GsonBuilder registerInstanceCreator(Type typeOfT,
      InstanceCreator<? extends T> instanceCreator) {
    instanceCreators.put(typeOfT, instanceCreator);
    return this;
  }

  /**
   * Register a custom JSON serializer for the specified class. You
   * should use this method if you want to register a common serializer
   * for all generic types corresponding to a raw type. If you want
   * different handling for different generic types corresponding
   * to a raw type, use {@link #registerSerializer(Type, JsonSerializer)}
   * instead.
   *
   * @param <T> the type for which the serializer is being registered
   * @param classOfT The class definition for the type T
   * @param serializer the custom serializer
   */
  public <T> GsonBuilder registerSerializer(Class<T> classOfT, JsonSerializer<T> serializer) {
    return registerSerializer((Type) classOfT, serializer);
  }

  /**
   * Register a custom JSON serializer for the specified type. You
   * should use this method if you want to register different
   * serializers for different generic types corresponding to a raw
   * type. If you want common handling for all generic types corresponding
   * to a raw type, use {@link #registerSerializer(Class, JsonSerializer)}
   * instead.
   *
   * @param <T> the type for which the serializer is being registered
   * @param typeOfT The type definition for T
   * @param serializer the custom serializer
   */
  public <T> GsonBuilder registerSerializer(Type typeOfT, final JsonSerializer<T> serializer) {
    serializers.put(typeOfT, serializer);
    return this;
  }
  
  /**
   * Register a custom JSON deserializer for the specified class. You
   * should use this method if you want to register a common deserializer
   * for all generic types corresponding to a raw type. If you want
   * different handling for different generic types corresponding
   * to a raw type, use {@link #registerDeserializer(Type, JsonDeserializer)}
   * instead.
   *
   * @param <T> the type for which the deserializer is being registered
   * @param classOfT The class definition for the type T
   * @param deserializer the custom deserializer
   */
  public <T> GsonBuilder registerDeserializer(Class<T> classOfT, JsonDeserializer<T> deserializer) {
    return registerDeserializer((Type) classOfT, deserializer);
  }
  
  /**
   * Register a custom JSON deserializer for the specified type. You
   * should use this method if you want to register different
   * deserializers for different generic types corresponding to a raw
   * type. If you want common handling for all generic types corresponding
   * to a raw type, use {@link #registerDeserializer(Class, JsonDeserializer)}
   * instead.
   *
   * @param <T> the type for which the deserializer is being registered
   * @param typeOfT The type definition for T
   * @param deserializer the custom deserializer
   */
  public <T> GsonBuilder registerDeserializer(Type typeOfT, final JsonDeserializer<T> deserializer) {
    deserializers.put(typeOfT, deserializer);
    return this;
  }
  
  /**
   * @return an instance of Gson configured with the parameters set 
   *         in this builder
   */
  public Gson create() {
    List<ExclusionStrategy> strategies = Lists.newArrayList(
        innerClassExclusionStrategy,
        modifierBasedExclusionStrategy);
    if (ignoreVersionsAfter != VersionConstants.IGNORE_VERSIONS) {
      strategies.add(new VersionExclusionStrategy(ignoreVersionsAfter));
    }
    ExclusionStrategy exclusionStrategy = new DisjunctionExclusionStrategy(strategies);
    ObjectNavigatorFactory objectNavigatorFactory = new ObjectNavigatorFactory(exclusionStrategy);
    MappedObjectConstructor objectConstructor = new MappedObjectConstructor();
    Gson gson = new Gson(objectNavigatorFactory, objectConstructor, typeAdapter, formatter);
    
    for (Map.Entry<Type, JsonSerializer<?>> entry : serializers.entrySet()) {
      gson.registerSerializer(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<Type, JsonDeserializer<?>> entry : deserializers.entrySet()) {
      gson.registerDeserializer(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<Type, InstanceCreator<?>> entry : instanceCreators.entrySet()) {
      gson.registerInstanceCreator(entry.getKey(), entry.getValue());
    }
    return gson;
  }
}
