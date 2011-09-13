/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl;

import javax.ws.rs.WebApplicationException;

/**
 * Life cycle wrapper for JAX-RS component (resource or provider).
 */
public final class LifecycleComponent
{
   /** State of component. */
   private enum State {
      INITIALIZED, DESTROYED;
   }

   private static final LifecycleMethodStrategy defaultStrategy = new AnnotatedLifecycleMethodStrategy();

   private final Object component;
   private final LifecycleMethodStrategy lifecycleStrategy;

   private State state;

   public LifecycleComponent(Object component)
   {
      this(component, defaultStrategy);
   }

   public LifecycleComponent(Object component, LifecycleMethodStrategy lifecycleStrategy)
   {
      this.component = component;
      this.lifecycleStrategy = lifecycleStrategy;
   }

   /**
    * Get target JAX-RS component.
    * 
    * @return target JAX-RS component
    */
   public Object getComponent()
   {
      return component;
   }

   /**
    * Call "initialize" method on the JAX-RS component. It is up to the implementation of LifecycleMethodStrategy how to
    * find "initialize" method. This method must be called once. It is possible to have more than one method of
    * initialization but any particular order of methods invocation is not guaranteed. Any exception was thrown by
    * "initialize" method must be wrapped by {@link InternalException}. Exception to the rule is
    * {@link javax.ws.rs.WebApplicationException}. It must be propagated to the caller.
    * 
    * @throws InternalException if "initialize" method throws an exception
    * @throws javax.ws.rs.WebApplicationException if "initialize" method throws WebApplicationException
    * @see #isInitialized()
    */
   public void initialize()
   {
      lifecycleStrategy.invokeInitializeMethods(getComponent());
      state = State.INITIALIZED;
   }

   /**
    * Call "destroy" method on the JAX-RS component. It is up to the implementation of LifecycleMethodStrategy how to
    * find "destroy" method. This method must be called once. It is possible to have more than one "destroy" method but
    * any particular order of methods invocation is not guaranteed. Any exception was thrown by "destroy" method must be
    * wrapped by {@link InternalException}. Exception to the rule is {@link javax.ws.rs.WebApplicationException}. It
    * must be propagated to the caller.
    * 
    * @throws InternalException if "destroy" method throws an exception
    * @throws javax.ws.rs.WebApplicationException if "destroy" method throws WebApplicationException
    * @see #isDestroyed()
    */
   public void destroy()
   {
      try
      {
         lifecycleStrategy.invokeDestroyMethods(getComponent());
      }
      finally
      {
         state = State.DESTROYED;
      }
   }

   /**
    * Check is component already initialized.
    * 
    * @return <code>true</code> if component already initialized but not destroyed yet and <code>false</code> otherwise
    * @see #initialize()
    */
   public boolean isInitialized()
   {
      return state == State.INITIALIZED;
   }

   /**
    * Check is component already destroyed.
    * 
    * @return <code>true</code> if component already destroyed and <code>false</code> otherwise
    * @see #destroy()
    */
   public boolean isDestroyed()
   {
      return state == State.DESTROYED;
   }

   public static interface LifecycleMethodStrategy
   {
      /**
       * Call "initialize" method on the specified object. It is up to the implementation how to find "initialize"
       * method. It is possible to have more than one initialize method but any particular order of methods invocation
       * is not guaranteed.
       * 
       * @param o the object
       * @throws WebApplicationException if initialize method throws WebApplicationException
       * @throws InternalException if initialize method throws any other exception
       */
      void invokeInitializeMethods(Object o);

      /**
       * Call "destroy" method on the specified object. It is up to the implementation how to find "destroy" method. It
       * is possible to have more than one destroy method but any particular order of methods invocation is not
       * guaranteed.
       * 
       * @param o the object
       * @throws WebApplicationException if destroy method throws WebApplicationException
       * @throws InternalException if destroy method throws any other exception
       */
      void invokeDestroyMethods(Object o);
   }
}