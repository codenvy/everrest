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

package org.everrest.spring;

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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * This loader registers any bean annotated with &#64;Path, &#64;Provider or
 * &#64;Filter in the EverRest framework.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SpringComponentsLoader implements BeanFactoryPostProcessor
{

   protected ResourceBinder resources;

   protected ApplicationProviderBinder providers;

   protected SpringComponentsLoader()
   {
   }

   public SpringComponentsLoader(ResourceBinder resources, ApplicationProviderBinder providers)
   {
      this.resources = resources;
      this.providers = providers;
   }

   @SuppressWarnings("unchecked")
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
   {
      ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
      for (String beanName : beanFactory.getBeanDefinitionNames())
      {
         Class<?> beanClass = beanFactory.getType(beanName);
         if (beanClass.getAnnotation(Provider.class) != null)
         {
            Object bean = beanFactory.getBean(beanName);
            if (bean instanceof ContextResolver)
               getProviders().addContextResolver((ContextResolver)bean);
            if (bean instanceof ExceptionMapper)
               getProviders().addExceptionMapper((ExceptionMapper)bean);
            if (bean instanceof MessageBodyReader)
               getProviders().addMessageBodyReader((MessageBodyReader)bean);
            if (bean instanceof MessageBodyWriter)
               getProviders().addMessageBodyWriter((MessageBodyWriter)bean);
         }
         else if (beanClass.getAnnotation(Filter.class) != null)
         {
            Object bean = beanFactory.getBean(beanName);
            if (bean instanceof MethodInvokerFilter)
               getProviders().addMethodInvokerFilter((MethodInvokerFilter)bean);
            if (bean instanceof RequestFilter)
               getProviders().addRequestFilter((RequestFilter)bean);
            if (bean instanceof ResponseFilter)
               getProviders().addResponseFilter((ResponseFilter)bean);
         }
         else if (beanClass.getAnnotation(Path.class) != null)
         {
            AbstractResourceDescriptor descriptor =
               new AbstractResourceDescriptorImpl(beanClass, ComponentLifecycleScope.IoC_CONTAINER);
            descriptor.accept(rdv);
            getResources().addResource(
               new SpringObjectFactory<AbstractResourceDescriptor>(descriptor, beanName, beanFactory));
         }
      }
   }

   protected ResourceBinder getResources()
   {
      return resources;
   }

   protected ApplicationProviderBinder getProviders()
   {
      return providers;
   }

}