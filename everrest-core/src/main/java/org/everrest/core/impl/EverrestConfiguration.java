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
package org.everrest.core.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
public class EverrestConfiguration {
    public static final String EVERREST_HTTP_METHOD_OVERRIDE      = "org.everrest.http.method.override";
    public static final String EVERREST_NORMALIZE_URI             = "org.everrest.normalize.uri";
    public static final String EVERREST_CHECK_SECURITY            = "org.everrest.security";
    public static final String EVERREST_ASYNCHRONOUS              = "org.everrest.asynchronous";
    public static final String EVERREST_ASYNCHRONOUS_SERVICE_PATH = "org.everrest.asynchronous.service.path";
    public static final String EVERREST_ASYNCHRONOUS_POOL_SIZE    = "org.everrest.asynchronous.pool.size";
    public static final String EVERREST_ASYNCHRONOUS_QUEUE_SIZE   = "org.everrest.asynchronous.queue.size";
    public static final String EVERREST_ASYNCHRONOUS_CACHE_SIZE   = "org.everrest.asynchronous.cache.size";
    public static final String EVERREST_ASYNCHRONOUS_JOB_TIMEOUT  = "org.everrest.asynchronous.job.timeout";
    public static final String METHOD_INVOKER_DECORATOR_FACTORY   = "org.everrest.core.impl.method.MethodInvokerDecoratorFactory";
    /**
     * Max buffer size configuration parameter. Entities that has size greater then specified will be stored in temporary directory on file
     * system during entity processing.
     */
    public static final String EVERREST_MAX_BUFFER_SIZE           = "org.everrest.max.buffer.size";


    public static final boolean defaultCheckSecurity           = true;
    public static final boolean defaultHttpMethodOverride      = true;
    public static final boolean defaultNormalizeUri            = false;
    public static final boolean defaultAsynchronousSupported   = true;
    public static final int     defaultAsynchronousPoolSize    = 10;
    public static final String  defaultAsynchronousServicePath = "/async";
    public static final int     defaultAsynchronousQueueSize   = 100;
    public static final int     defaultAsynchronousCacheSize   = 512;
    public static final int     defaultAsynchronousJobTimeout  = 60;
    /** Max buffer size attribute value. See {@link #EVERREST_MAX_BUFFER_SIZE}. */
    public static final int     defaultMaxBufferSize           = 204800;

    protected final Map<String, String> properties;

    public EverrestConfiguration() {
        properties = new HashMap<>();
    }

    public EverrestConfiguration(EverrestConfiguration other) {
        properties = new HashMap<>(other.getAllProperties());
    }

    public Map<String, String> getAllProperties() {
        return new HashMap<>(properties);
    }

    public boolean isCheckSecurity() {
        return getBooleanProperty(EVERREST_CHECK_SECURITY, defaultCheckSecurity);
    }

    public void setCheckSecurity(boolean checkSecurity) {
        properties.put(EVERREST_CHECK_SECURITY, Boolean.toString(checkSecurity));
    }

    public boolean isHttpMethodOverride() {
        return getBooleanProperty(EVERREST_HTTP_METHOD_OVERRIDE, defaultHttpMethodOverride);
    }

    public void setHttpMethodOverride(boolean httpMethodOverride) {
        properties.put(EVERREST_HTTP_METHOD_OVERRIDE, Boolean.toString(httpMethodOverride));
    }

    public boolean isNormalizeUri() {
        return getBooleanProperty(EVERREST_NORMALIZE_URI, defaultNormalizeUri);
    }

    public void setNormalizeUri(boolean normalizeUri) {
        properties.put(EVERREST_NORMALIZE_URI, Boolean.toString(normalizeUri));
    }

    public boolean isAsynchronousSupported() {
        return getBooleanProperty(EVERREST_ASYNCHRONOUS, defaultAsynchronousSupported);
    }

    public void setAsynchronousSupported(boolean asynchronousSupported) {
        properties.put(EVERREST_ASYNCHRONOUS, Boolean.toString(asynchronousSupported));
    }

    public String getAsynchronousServicePath() {
        return getProperty(EVERREST_ASYNCHRONOUS_SERVICE_PATH, defaultAsynchronousServicePath);
    }

    public void setAsynchronousServicePath(String servicePath) {
        properties.put(EVERREST_ASYNCHRONOUS_SERVICE_PATH, servicePath);
    }

    public int getAsynchronousPoolSize() {
        return getNumberProperty(EVERREST_ASYNCHRONOUS_POOL_SIZE, defaultAsynchronousPoolSize).intValue();
    }

    public void setAsynchronousPoolSize(int asynchronousPoolSize) {
        properties.put(EVERREST_ASYNCHRONOUS_POOL_SIZE, Integer.toString(asynchronousPoolSize));
    }

    public int getAsynchronousQueueSize() {
        return getNumberProperty(EVERREST_ASYNCHRONOUS_QUEUE_SIZE, defaultAsynchronousQueueSize).intValue();
    }

    public void setAsynchronousQueueSize(int asynchronousQueueSize) {
        properties.put(EVERREST_ASYNCHRONOUS_QUEUE_SIZE, Integer.toString(asynchronousQueueSize));
    }

    public int getAsynchronousCacheSize() {
        return getNumberProperty(EVERREST_ASYNCHRONOUS_CACHE_SIZE, defaultAsynchronousCacheSize).intValue();
    }

    public void setAsynchronousCacheSize(int asynchronousCacheSize) {
        properties.put(EVERREST_ASYNCHRONOUS_CACHE_SIZE, Integer.toString(asynchronousCacheSize));
    }

    public int getAsynchronousJobTimeout() {
        return getNumberProperty(EVERREST_ASYNCHRONOUS_JOB_TIMEOUT, defaultAsynchronousJobTimeout).intValue();
    }

    public void setAsynchronousJobTimeout(int asynchronousJobTimeout) {
        properties.put(EVERREST_ASYNCHRONOUS_JOB_TIMEOUT, Integer.toString(asynchronousJobTimeout));
    }

    public int getMaxBufferSize() {
        return getNumberProperty(EVERREST_MAX_BUFFER_SIZE, defaultMaxBufferSize).intValue();
    }

    public void setMaxBufferSize(int maxBufferSize) {
        properties.put(EVERREST_MAX_BUFFER_SIZE, Integer.toString(maxBufferSize));
    }

    public void setProperty(String name, String value) {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public String getProperty(String name, String def) {
        String value = getProperty(name);
        if (value == null) {
            return def;
        }
        return value;
    }

    public boolean getBooleanProperty(String name, boolean def) {
        String str = getProperty(name);
        if (str != null) {
            return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str) || "1".equals(str);
        }
        return def;
    }

    public Double getNumberProperty(String name, double def) {
        String str = getProperty(name);
        if (str != null) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }
}
