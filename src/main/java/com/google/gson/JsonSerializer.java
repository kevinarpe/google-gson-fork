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

/**
 * Interface representing a custom serializer for Json.   
 * 
 * @author Inderjeet Singh
 *
 * @param <T> type for which the serializer is being registered. It is possible
 * that a serializer may be asked to serialize a specific generic type of the
 * T. 
 */
public interface JsonSerializer<T> {
    
  /**
   * @param src the object that needs to be converted to Json
   * @return a JsonElement corresponding to the specified object 
   */
  public JsonElement toJson(T src);
}
