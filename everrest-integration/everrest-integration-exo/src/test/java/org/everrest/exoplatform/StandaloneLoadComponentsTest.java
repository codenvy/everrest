/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.exoplatform;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author andrew00x
 */
public class StandaloneLoadComponentsTest extends StandaloneBaseTest {
    @Test
    public void testLoadComponents() {
        assertNotNull(dependencySupplier);
        assertNotNull(providers);
        assertNotNull(resources);
        assertNotNull(processor);
    }
}
