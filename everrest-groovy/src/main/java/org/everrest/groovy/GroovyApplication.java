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

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Define names of Groovy scripts which should be components of a JAX-RS
 * application.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class GroovyApplication extends Application
{
   private static final Set<Class<?>> classes = Collections.emptySet();

   @Override
   public Set<Class<?>> getClasses()
   {
      return classes;
   }

   /**
    * Get names (FQN) of scripts which should be components of a JAX-RS
    * application. All components (resources and providers) got per-request
    * lifecycle.
    * 
    * @return set of names of Groovy scripts. Returning null is equivalent to
    *         returning an empty set.
    */
   public abstract Set<String> getScripts();

}
