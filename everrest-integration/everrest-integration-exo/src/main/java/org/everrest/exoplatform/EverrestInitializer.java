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

import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollector;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.InternalException;
import org.everrest.core.impl.LifecycleComponent;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Load all available instances of {@link Application} (include {@link StartableApplication}) from ExoContainer and
 * deploy them. EverrestInitializer should be used when components of EverRest framework registered in ExoContainer.
 * Process of ExoContainer bootstrap is opaque for EverrestInitializer.
 * <p/>
 * It is NOT expected to use EverrestInitializer and sub-classes of
 * {@link org.everrest.exoplatform.servlet.EverrestExoContextListener} in same web application. Use
 * {@link org.everrest.exoplatform.servlet.EverrestExoServlet} instead of
 * {@link org.everrest.core.servlet.EverrestServlet}.
 *
 * @author andrew00x
 */
public class EverrestInitializer implements Startable {
    private static final Logger LOG = LoggerFactory.getLogger(EverrestInitializer.class);

    private final ExoContainer                container;
    private final EverrestConfiguration       config;
    private final EverrestProcessor           processor;
    private       List<WeakReference<Object>> singletonsReferences;

    public EverrestInitializer(ExoContainerContext containerContext,
                               EverrestProcessor processor,
                               StartableApplication eXo /* Be sure eXo components are initialized. */,
                               InitParams initParams) {
        this.processor = processor;
        this.container = containerContext.getContainer();
        this.config = EverrestConfigurationHelper.createEverrestConfiguration(initParams);
    }

    @Override
    public void start() {
        EverrestApplication everrest = new EverrestApplication();
        if (config.isAsynchronousSupported()) {
            everrest.addSingleton(new ExoAsynchronousJobPool(config));
            everrest.addSingleton(new AsynchronousProcessListWriter());
            everrest.addClass(AsynchronousJobService.class);
        }
        if (config.isCheckSecurity()) {
            everrest.addSingleton(new SecurityConstraint());
        }

        // Do not prevent GC remove objects if they are removed somehow from ResourceBinder or ProviderBinder.
        // NOTE We provider life cycle control ONLY for internal components and do nothing for components
        // obtained from container. Container must take care about its components.
        Set<Object> singletons = everrest.getSingletons();
        singletonsReferences = new ArrayList<>(singletons.size());
        for (Object o : singletons) {
            singletonsReferences.add(new WeakReference<>(o));
        }
        processor.addApplication(everrest);
        // Process applications.
        List allApps = container.getComponentInstancesOfType(Application.class);
        if (allApps != null && !allApps.isEmpty()) {
            for (Object o : allApps) {
                processor.addApplication((Application)o);
            }
        }
    }

    @Override
    public void stop() {
        makeFileCollectorDestroyer().stopFileCollector();
        if (singletonsReferences != null && singletonsReferences.size() > 0) {
            for (WeakReference<Object> ref : singletonsReferences) {
                Object o = ref.get();
                if (o != null) {
                    try {
                        new LifecycleComponent(o).destroy();
                    } catch (InternalException e) {
                        LOG.error("Unable to destroy component. ", e);
                    }
                }
            }
            singletonsReferences.clear();
        }
    }

    protected FileCollectorDestroyer makeFileCollectorDestroyer() {
        // Always stop FileCollector without checking FileCollector class loader.
        return new FileCollectorDestroyer() {
            @Override
            public void stopFileCollector() {
                FileCollector fc = FileCollector.getInstance();
                fc.stop();
            }
        };
    }
}
