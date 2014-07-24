/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core;

/**
 * Implementation of this interface determine name of dependency required for constructors or fields of Resource or
 * Provider. Typically the name of the dependency may be set by annotation with value as component's qualifier, e.g.
 * &#064javax.inject.Named.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface DependencyNameDetector {
    /**
     * Get name of parameter.
     *
     * @param parameter
     *         the Parameter
     * @return name of <code>parameter</code> or <code>null</code> if name can't be detected by particular
     *         DependencyNameDetector
     */
    String getName(Parameter parameter);
}
