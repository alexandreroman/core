/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.webbeans.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedMethod;

import com.google.common.collect.ForwardingMap;

/**
 * Represents an annotated annotation
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class AnnotatedAnnotationImpl<T extends Annotation> extends AbstractAnnotatedType<T> implements AnnotatedAnnotation<T>
{

   /**
    * A (annotation type -> set of method abstractions with annotation) map
    */
   private class AnnotatedMembers extends ForwardingMap<Class<? extends Annotation>, Set<AnnotatedMethod<?>>>
   {
      private Map<Class<? extends Annotation>, Set<AnnotatedMethod<?>>> delegate;

      public AnnotatedMembers()
      {
         delegate = new HashMap<Class<? extends Annotation>, Set<AnnotatedMethod<?>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, Set<AnnotatedMethod<?>>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("Annotation type -> member abstraction mappings: " + super.size() + "\n");
         int i = 0;
         for (Entry<Class<? extends Annotation>, Set<AnnotatedMethod<?>>> entry : delegate.entrySet())
         {
            for (AnnotatedMethod<?> parameter : entry.getValue())
            {
               buffer.append(++i + " - " + entry.getKey().toString() + ": " + parameter.toString() + "\n");
            }
         }
         return buffer.toString();
      }
   }

   // The annotated members map (annotation -> member with annotation)
   private AnnotatedMembers annotatedMembers;
   // The implementation class of the annotation
   private Class<T> clazz;
   // The set of abstracted members
   private Set<AnnotatedMethod<?>> members;

   /**
    * Constructor
    * 
    * Initializes the superclass with the built annotation map
    * 
    * @param annotationType The annotation type
    */
   public AnnotatedAnnotationImpl(Class<T> annotationType)
   {
      super(buildAnnotationMap(annotationType));
      this.clazz = annotationType;
   }

   /**
    * Gets the actual type arguments
    * 
    * @return The type arguments
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getActualTypeArguments()
    */
   public Type[] getActualTypeArguments()
   {
      return new Type[0];
   }

   /**
    * Gets all members of the annotation
    * 
    * Initializes the members first if they are null
    * 
    * @return The set of abstracted members
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getMembers()
    */
   public Set<AnnotatedMethod<?>> getMembers()
   {
      if (members == null)
      {
         initMembers();
      }
      return members;
   }

   /**
    * Gets the delegate
    * 
    * @return The delegate
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getDelegate()
    */
   public Class<T> getDelegate()
   {
      return clazz;
   }

   /**
    * Gets the type of the annotation
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getType()
    */
   public Class<T> getType()
   {
      return clazz;
   }

   /**
    * Initializes the members
    * 
    * Iterates through the declared members, creates abstractions of them and
    * adds them to the members set
    */
   private void initMembers()
   {
      members = new HashSet<AnnotatedMethod<?>>();
      for (Method member : clazz.getDeclaredMethods())
      {
         members.add(new AnnotatedMethodImpl<Object>(member, this));
      }
   }

   /**
    * Returns the annotated members with a given annotation type
    * 
    * If the annotated members are null, they are initialized first.
    * 
    * @param annotationType The annotation type to match
    * @return The set of abstracted members with the given annotation type
    *         present. An empty set is returned if no matches are found
    *         
    * @see org.jboss.webbeans.introspector.AnnotatedAnnotation#getAnnotatedMembers(Class)
    */
   public Set<AnnotatedMethod<?>> getAnnotatedMembers(Class<? extends Annotation> annotationType)
   {
      if (annotatedMembers == null)
      {
         initAnnotatedMembers();
      }

      if (!annotatedMembers.containsKey(annotationType))
      {
         return new HashSet<AnnotatedMethod<?>>();
      }
      else
      {
         return annotatedMembers.get(annotationType);
      }
   }

   /**
    * Initializes the annotated members
    * 
    * If the members are null, the are initialized first.
    * 
    * Iterates over the members and for each member, iterate over the
    * annotations present, creating member abstractions and mapping them under
    * the annotation in the annotatedMembers map.
    */
   private void initAnnotatedMembers()
   {
      if (members == null)
      {
         initMembers();
      }
      annotatedMembers = new AnnotatedMembers();
      for (AnnotatedMethod<?> member : members)
      {
         for (Annotation annotation : member.getAnnotations())
         {
            if (!annotatedMembers.containsKey(annotation))
            {
               annotatedMembers.put(annotation.annotationType(), new HashSet<AnnotatedMethod<?>>());
            }
            annotatedMembers.get(annotation.annotationType()).add(member);
         }
      }
   }

   /**
    * Gets a string representation of the constructor
    * 
    * @return A string representation
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
//      buffer.append("AnnotatedConstructorImpl:\n");
//      buffer.append(super.toString() + "\n");
//      buffer.append("Class: " + clazz.toString() + "\n");
//
//      buffer.append("Members: " + getMembers().size() + "\n");
//      int i = 0;
//      for (AnnotatedMethod<?> member : getMembers())
//      {
//         buffer.append(++i + " - " + member.toString());
//      }
//      buffer.append(annotatedMembers == null ? "" : (annotatedMembers.toString() + "\n"));
      return buffer.toString();
   }

}
