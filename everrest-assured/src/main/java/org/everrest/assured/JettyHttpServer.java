/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.everrest.assured;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;
import org.everrest.assured.util.AvailablePortFinder;
import org.everrest.assured.util.IoUtil;
import org.everrest.core.DependencySupplier;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.RestComponentResolver;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.servlet.EverrestInitializedListener;
import org.everrest.core.servlet.EverrestServlet;
import org.everrest.groovy.BaseResourceId;
import org.everrest.groovy.GroovyResourcePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.Random;

public class JettyHttpServer {

    public static final  String UNSECURE_REST         = "/rest";
    public static final  String UNSECURE_PATH_SPEC    = UNSECURE_REST + "/*";
    public static final  String SECURE_PATH           = "/private";
    public static final  String SECURE_REST           = UNSECURE_REST + SECURE_PATH;
    public static final  String SECURE_PATH_SPEC      = SECURE_REST + "/*";
    public final static  String ADMIN_USER_NAME       = "cldadmin";
    public final static  String ADMIN_USER_PASSWORD   = "tomcat";
    public final static  String MANAGER_USER_NAME     = "cldmanager";
    public final static  String MANAGER_USER_PASSWORD = "manager";
    public final static  String UNAUTHORIZED_USER     = "user";
    private static final Logger LOG                   = LoggerFactory.getLogger(JettyHttpServer.class);
    private final static Random portRandomizer        = new Random();
    protected final int                   port;
    protected final Server                server;
    protected       ServletContextHandler context;

    /**
     *
     */
    public JettyHttpServer() {
        this(AvailablePortFinder.getNextAvailable(10000 + portRandomizer.nextInt(2000)));
    }

    public JettyHttpServer(int port) {
        this.port = port;
        this.server = new Server(port);
        this.context = null;
    }

    public int getPort() {
        return port;
    }

    public void start() throws Exception {
        RequestLogHandler handler = new RequestLogHandler();

        if (context == null) {
            context = new ServletContextHandler(handler, "/", ServletContextHandler.SESSIONS);
        }

        context.setEventListeners(new EventListener[]{new EverrestInitializedListener()});
        ServletHolder servletHolder = new ServletHolder(new EverrestServlet());

        context.addServlet(servletHolder, UNSECURE_PATH_SPEC);
        context.addServlet(servletHolder, SECURE_PATH_SPEC);

        //set up security
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"cloud-admin", "users", "user"});
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec(SECURE_PATH_SPEC);

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(constraintMapping);

        HashLoginService loginService = new HashLoginService();
        loginService.putUser(ADMIN_USER_NAME, new Password(ADMIN_USER_PASSWORD),
                             new String[]{"cloud-admin",
                                          "users",
                                          "user",
                                          "developer",
                                          "admin",
                                          "workspace/developer",
                                          "workspace/admin",
                                          "account/owner",
                                          "account/member",
                                          "system/admin",
                                          "system/manager"
                             });
        loginService.putUser(MANAGER_USER_NAME, new Password(MANAGER_USER_PASSWORD), new String[]{"cloud-admin",
                                                                                                  "user",
                                                                                                  "users"});

        securityHandler.setLoginService(loginService);
        securityHandler.setAuthenticator(new BasicAuthenticator());

        context.setSecurityHandler(securityHandler);

        server.setHandler(handler);

        server.start();
        ResourceBinder binder =
                (ResourceBinder)context.getServletContext().getAttribute(ResourceBinder.class.getName());
        DependencySupplier dependencies =
                (DependencySupplier)context.getServletContext().getAttribute(DependencySupplier.class.getName());
        GroovyResourcePublisher groovyPublisher = new GroovyResourcePublisher(binder, dependencies);
        context.getServletContext().setAttribute(GroovyResourcePublisher.class.getName(), groovyPublisher);

    }

    public void stop() throws Exception {
        context = null;
        server.stop();

    }

    public void addUser(String userName, Credential credential, String[] roles) {
        ((HashLoginService)context.getSecurityHandler().getLoginService()).putUser(userName, credential, roles);
    }

    public void addSingleton(Object instance) {
        LOG.debug("addSingleton {}", instance.getClass());
        ResourceBinder resources = (ResourceBinder)context.getServletContext().getAttribute(ResourceBinder.class.getName());
        ProviderBinder providers = (ProviderBinder)context.getServletContext().getAttribute(ApplicationProviderBinder.class.getName());
        RestComponentResolver resolver = new RestComponentResolver(resources, providers);
        resolver.addSingleton(instance);
    }

    public void addPerRequest(Class clazz) {
        LOG.debug("addPerRequest << {}", clazz);
        ResourceBinder resources = (ResourceBinder)context.getServletContext().getAttribute(ResourceBinder.class.getName());
        ProviderBinder providers = (ProviderBinder)context.getServletContext().getAttribute(ApplicationProviderBinder.class.getName());
        RestComponentResolver resolver = new RestComponentResolver(resources, providers);
        resolver.addPerRequest(clazz);
    }

    public void addFactory(ObjectFactory<ResourceDescriptor> factory) {
        ResourceBinder binder = (ResourceBinder)context.getServletContext().getAttribute(ResourceBinder.class.getName());
        binder.addResource(factory);
    }


    public void publishPerRequestGroovyScript(String resourcePath, String name) {
        GroovyResourcePublisher groovyPublisher =
                (GroovyResourcePublisher)context.getServletContext().getAttribute(GroovyResourcePublisher.class.getName());

        BaseResourceId publishedResourceId = new BaseResourceId(name);
        groovyPublisher.publishPerRequest(IoUtil.getResource(resourcePath), publishedResourceId, null, null, null);
    }

    public void addFilter(Filter filter, String pathSpec) {
        context.addFilter(new FilterHolder(filter), pathSpec, null);
    }

    public void addFilter(Class<? extends Filter> filterClass, String pathSpec) {
        context.addFilter(filterClass, pathSpec, null);
    }


    public void resetFilter() {
        context.getServletHandler().setFilters(null);

        try {

            Field filterMappings = ServletHandler.class.getDeclaredField("_filterMappings");
            filterMappings.setAccessible(true);
            filterMappings.set(context.getServletHandler(), null);


            Method updateMappingsMethod = ServletHandler.class.getDeclaredMethod("updateMappings");
            updateMappingsMethod.setAccessible(true);
            updateMappingsMethod.invoke(context.getServletHandler());
        } catch (ReflectiveOperationException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /** @return the context */
    public ServletContextHandler getContext() {
        return context;
    }

    public void resetFactories() {
        LOG.debug("reset >>");
        ResourceBinder binder = (ResourceBinder)context.getServletContext().getAttribute(ResourceBinder.class.getName());
        ((ResourceBinderImpl)binder).clear();
        ProviderBinder.setInstance(null);

    }


}
