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

package org.everrest.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

import org.everrest.core.DependencySupplier;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.ResourceBinder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage via {@link ResourceBinder} Groovy based RESTful services.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: GroovyJaxrsPublisher.java 2663 2010-06-18 13:50:27Z aparfonov $
 */
public class GroovyJaxrsPublisher
{

   /** Default character set name. */
   protected static final String DEFAULT_CHARSET_NAME = "UTF-8";

   /** Default character set. */
   protected static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

   protected final ResourceBinder binder;

   protected GroovyClassLoader gcl;

   protected final Map<ResourceId, Class<?>> resources =
      Collections.synchronizedMap(new HashMap<ResourceId, Class<?>>());

   protected final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new Comparator<Constructor<?>>()
   {
      public int compare(Constructor<?> o1, Constructor<?> o2)
      {
         int r = o2.getParameterTypes().length - o1.getParameterTypes().length;
         return r;
      }
   };

   protected final DependencySupplier dependencies;

   /**
    * Create GroovyJaxrsPublisher which is able publish per-request and
    * singleton resources. Any required dependencies for per-request resource
    * injected by {@link PerRequestObjectFactory}, instance of singleton
    * resources will be created by {@link GroovyScriptInstantiator}.
    * 
    * @param binder resource binder
    * @param instantiator instantiate java object from given groovy source
    */
   public GroovyJaxrsPublisher(ResourceBinder binder, DependencySupplier dependencies)
   {
      this.binder = binder;
      this.dependencies = dependencies;
      ClassLoader cl = getClass().getClassLoader();
      this.gcl = new GroovyClassLoader(cl);
   }

   /**
    * @return get underling groovy class loader
    */
   public GroovyClassLoader getGroovyClassLoader()
   {
      return gcl;
   }

   /**
    * Check is groovy resource with specified id is published or not
    * 
    * @param resourceId id of resource to be checked
    * @return <code>true</code> if resource is published and <code>false</code>
    *         otherwise
    */
   public boolean isPublished(ResourceId resourceId)
   {
      return resources.containsKey(resourceId);
   }

   /**
    * Parse given stream and publish result as per-request RESTful service.
    * 
    * @param in stream which contains groovy source code of RESTful service
    * @param resourceId id to be assigned to resource
    * @return <code>true</code> if resource was published and <code>false</code>
    *         otherwise
    * @throws NullPointerException if <code>resourceId == null</code>
    */
   public boolean publishPerRequest(InputStream in, ResourceId resourceId)
   {
      Class<?> rc = gcl.parseClass(createCodeSource(in, resourceId.getId()));
      boolean answ = binder.bind(rc);
      if (answ)
      {
         resources.put(resourceId, rc);
      }
      return answ;
   }

   /**
    * Parse given <code>source</code> and publish result as per-request RESTful
    * service.
    * 
    * @param source groovy source code of RESTful service
    * @param resourceId id to be assigned to resource
    * @return <code>true</code> if resource was published and <code>false</code>
    *         otherwise
    * @throws NullPointerException if <code>resourceId == null</code>
    */
   public final boolean publishPerRequest(String source, ResourceId resourceId)
   {
      return publishPerRequest(source, DEFAULT_CHARSET, resourceId);
   }

   /**
    * Parse given <code>source</code> and publish result as per-request RESTful
    * service.
    * 
    * @param source groovy source code of RESTful service
    * @param charset source string charset. May be <code>null</code> than
    *        default charset will be in use
    * @param resourceId id to be assigned to resource
    * @return <code>true</code> if resource was published and <code>false</code>
    *         otherwise
    * @throws UnsupportedCharsetException if <code>charset</code> is unsupported
    * @throws NullPointerException if <code>resourceId == null</code>
    */
   public final boolean publishPerRequest(String source, String charset, ResourceId resourceId)
   {
      return publishPerRequest(source, charset == null ? DEFAULT_CHARSET : Charset.forName(charset), resourceId);
   }

   /**
    * Parse given stream and publish result as singleton RESTful service.
    * 
    * @param in stream which contains groovy source code of RESTful service
    * @param resourceId id to be assigned to resource
    * @return <code>true</code> if resource was published and <code>false</code>
    *         otherwise
    * @throws NullPointerException if <code>resourceId == null</code>
    */
   public boolean publishSingleton(InputStream in, ResourceId resourceId)
   {
      Class<?> rc = gcl.parseClass(createCodeSource(in, resourceId.getId()));
      Object r = null;

      Constructor<?>[] constructors = rc.getConstructors();
      /* Sort constructors by number of parameters. With more parameters must be first. */
      Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);

      l : for (Constructor<?> c : constructors)
      {
         Class<?>[] parameterTypes = c.getParameterTypes();
         if (parameterTypes.length == 0)
         {
            try
            {
               r = c.newInstance();
               break;
            }
            catch (Exception e)
            {
               throw new RuntimeException("Unable instantiate object. " + e.getMessage(), e);
            }
         }

         List<Object> parameters = new ArrayList<Object>(parameterTypes.length);

         for (Class<?> parameterType : parameterTypes)
         {
            Object param = dependencies.getComponent(parameterType);
            if (param == null)
            {
               continue l;
            }
            parameters.add(param);
         }
         try
         {
            r = c.newInstance(parameters.toArray(new Object[parameters.size()]));
            break;
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unable instantiate object." + e.getMessage(), e);
         }
      }

      boolean answ = binder.bind(r);
      if (answ)
      {
         resources.put(resourceId, r.getClass());
      }
      return answ;
   }

   /**
    * Parse given <code>source</code> and publish result as singleton RESTful
    * service.
    * 
    * @param source groovy source code of RESTful service
    * @param resourceId name of resource
    * @return <code>true</code> if resource was published and <code>false</code>
    *         otherwise
    * @throws NullPointerException if <code>resourceId == null</code>
    */
   public final boolean publishSingleton(String source, ResourceId resourceId)
   {
      return publishSingleton(source, DEFAULT_CHARSET, resourceId);
   }

   /**
    * Parse given <code>source</code> and publish result as singleton RESTful
    * service.
    * 
    * @param source groovy source code of RESTful service
    * @param charset source string charset. May be <code>null</code> than
    *        default charset will be in use
    * @param resourceId name of resource
    * @return <code>true</code> if resource was published and <code>false</code>
    *         otherwise
    * @throws UnsupportedCharsetException if <code>charset</code> is unsupported
    * @throws NullPointerException if <code>resourceId == null</code>
    */
   public final boolean publishSingleton(String source, String charset, ResourceId resourceId)
   {
      return publishSingleton(source, charset == null ? DEFAULT_CHARSET : Charset.forName(charset), resourceId);
   }

   /**
    * Set groovy class loader.
    * 
    * @param gcl groovy class loader
    * @throws NullPointerException if <code>gcl == null</code>
    */
   public void setGroovyClassLoader(GroovyClassLoader gcl)
   {
      if (gcl == null)
         throw new NullPointerException("GroovyClassLoader may not be null.");
      this.gcl = gcl;
   }

   /**
    * Unpublish resource with specified id.
    * 
    * @param resourceId id of resource to be unpublished
    * @return <code>true</code> if resource was published and <code>false</code>
    *         otherwise, e.g. because there is not resource corresponded to
    *         supplied <code>resourceId</code>
    */
   public boolean unpublishResource(ResourceId resourceId)
   {
      Class<?> clazz = resources.get(resourceId);
      boolean answ = false;
      if (clazz != null)
      {
         answ = binder.unbind(clazz);
      }
      if (answ)
      {
         resources.remove(resourceId);
      }
      return answ;
   }

   private boolean publishPerRequest(String source, Charset charset, ResourceId resourceId)
   {
      byte[] bytes = source.getBytes(charset);
      return publishPerRequest(new ByteArrayInputStream(bytes), resourceId);
   }

   private boolean publishSingleton(String source, Charset charset, ResourceId resourceId)
   {
      byte[] bytes = source.getBytes(charset);
      return publishSingleton(new ByteArrayInputStream(bytes), resourceId);
   }

   /**
    * Create {@link GroovyCodeSource} from given stream and name. Code base
    * 'file:/groovy/script/jaxrs' will be used.
    * 
    * @param in groovy source code stream
    * @param name code source name
    * @return GroovyCodeSource
    */
   protected GroovyCodeSource createCodeSource(InputStream in, String name)
   {
      GroovyCodeSource gcs = new GroovyCodeSource(in, name, "/groovy/script/jaxrs");
      gcs.setCachable(false);
      return gcs;
   }
}
