/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.pico;

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.Filter;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.web.WebappComposer;

import java.util.Collection;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Register components of containers with different webapp scopes (application,
 * session, request) in EverRest framework if they are annotated with &#64;Path,
 * &#64;Provider or &#64;Filter annotation.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see WebappComposer
 */
public abstract class EverrestComposer implements WebappComposer
{

   protected ApplicationProviderBinder providers;

   protected ResourceBinder resources;

   public final void composeApplication(MutablePicoContainer container, ServletContext servletContext)
   {
      doComposeApplication(container, servletContext);
      setResources((ResourceBinder)servletContext.getAttribute(ResourceBinder.class.getName()));
      setProviders((ApplicationProviderBinder)servletContext.getAttribute(ApplicationProviderBinder.class.getName()));
      processComponents(container);
   }

   public final void composeRequest(MutablePicoContainer container)
   {
      doComposeRequest(container);
      processComponents(container);
   }

   public final void composeSession(MutablePicoContainer container)
   {
      doComposeSession(container);
      processComponents(container);
   }

   protected abstract void doComposeApplication(MutablePicoContainer container, ServletContext servletContext);

   protected abstract void doComposeRequest(MutablePicoContainer container);

   protected abstract void doComposeSession(MutablePicoContainer container);

   protected ApplicationProviderBinder getProviders()
   {
      return providers;
   }

   protected ResourceBinder getResources()
   {
      return resources;
   }

   @SuppressWarnings("unchecked")
   protected void processComponents(MutablePicoContainer container)
   {
      Collection<ComponentAdapter<?>> adapters = container.getComponentAdapters();
      ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
      for (ComponentAdapter<?> adapter : adapters)
      {
         Class<?> clazz = adapter.getComponentImplementation();
         if (clazz.getAnnotation(Provider.class) != null)
         {
            Object object = adapter.getComponentInstance(container, ComponentAdapter.NOTHING.class);
            if (object instanceof ContextResolver)
               getProviders().addContextResolver((ContextResolver)object);
            if (object instanceof ExceptionMapper)
               getProviders().addExceptionMapper((ExceptionMapper)object);
            if (object instanceof MessageBodyReader)
               getProviders().addMessageBodyReader((MessageBodyReader)object);
            if (object instanceof MessageBodyWriter)
               getProviders().addMessageBodyWriter((MessageBodyWriter)object);
         }
         else if (clazz.getAnnotation(Filter.class) != null)
         {
            Object object = adapter.getComponentInstance(container, ComponentAdapter.NOTHING.class);
            if (object instanceof MethodInvokerFilter)
               getProviders().addMethodInvokerFilter((MethodInvokerFilter)object);
            if (object instanceof RequestFilter)
               getProviders().addRequestFilter((RequestFilter)object);
            if (object instanceof ResponseFilter)
               getProviders().addResponseFilter((ResponseFilter)object);
         }
         else if (clazz.getAnnotation(Path.class) != null)
         {
            AbstractResourceDescriptor descriptor =
               new AbstractResourceDescriptorImpl(clazz, ComponentLifecycleScope.IoC_CONTAINER);
            descriptor.accept(rdv);
            getResources().addResource(new PicoObjectFactory<AbstractResourceDescriptor>(descriptor));
         }
      }
   }

   protected void setProviders(ApplicationProviderBinder providers)
   {
      this.providers = providers;
   }

   protected void setResources(ResourceBinder resources)
   {
      this.resources = resources;
   }

}
