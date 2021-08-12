/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.wadl;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import javax.xml.namespace.QName;
import org.everrest.core.Parameter;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.wadl.research.Application;
import org.everrest.core.wadl.research.Param;
import org.everrest.core.wadl.research.ParamStyle;
import org.everrest.core.wadl.research.RepresentationType;
import org.everrest.core.wadl.research.Resources;

/**
 * Base implementation of {@link WadlGenerator}. This implementation does not* provide doc and
 * grammar extension of WADL.
 *
 * @author andrew00x
 */
public class BaseWadlGeneratorImpl implements WadlGenerator {

  @Override
  public Application createApplication() {
    return new Application();
  }

  @Override
  public Resources createResources() {
    return new Resources();
  }

  @Override
  public org.everrest.core.wadl.research.Resource createResource(ResourceDescriptor rd) {
    if (rd.isRootResource()) {
      return createResource(rd.getPathValue().getPath());
    }
    return createResource((String) null);
  }

  @Override
  public org.everrest.core.wadl.research.Resource createResource(String path) {
    org.everrest.core.wadl.research.Resource wadlResource =
        new org.everrest.core.wadl.research.Resource();
    if (path != null) {
      wadlResource.setPath(path);
    }
    return wadlResource;
  }

  @Override
  public org.everrest.core.wadl.research.Method createMethod(ResourceMethodDescriptor rmd) {
    String httpMethod = rmd.getHttpMethod();
    // Ignore HEAD methods currently.
    // Implementation of wadl2java for generation client code does not support
    // HEAD method. See https://wadl.dev.java.net/ .
    // If WADL contains HEAD method description then client code get part of
    // code as next:
    // --------------------------------------------
    //    public DataSource headAs()
    //    throws IOException, MalformedURLException
    // {
    //    HashMap<String, Object> _queryParameterValues = new HashMap<String, Object>();
    //    HashMap<String, Object> _headerParameterValues = new HashMap<String, Object>();
    //    String _url = _uriBuilder.buildUri(_templateAndMatrixParameterValues, _queryParameterVal
    //    DataSource _retVal = _dsDispatcher.doHEAD(_url, _headerParameterValues, "*/*");
    //    return _retVal;
    // }
    // --------------------------------------------
    // But class  org.jvnet.ws.wadl.util.DSDispatcher doesn't have method doHEAD at all.
    //
    if (httpMethod.equals("HEAD")) {
      return null;
    }

    org.everrest.core.wadl.research.Method wadlMethod =
        new org.everrest.core.wadl.research.Method();
    wadlMethod.setName(httpMethod);
    java.lang.reflect.Method m = rmd.getMethod();
    // NOTE Method may be null in some cases. For example OPTIONS method
    // processor use null method and fake invoker. See
    // OptionsRequestResourceMethodDescriptorImpl.
    if (m != null) {
      wadlMethod.setId(m.getName());
    }
    return wadlMethod;
  }

  @Override
  public org.everrest.core.wadl.research.Request createRequest() {
    return new org.everrest.core.wadl.research.Request();
  }

  @Override
  public org.everrest.core.wadl.research.Response createResponse() {
    return new org.everrest.core.wadl.research.Response();
  }

  @Override
  public RepresentationType createRequestRepresentation(MediaType mediaType) {
    RepresentationType wadlRepresentation = new RepresentationType();
    wadlRepresentation.setMediaType(mediaType.toString());
    return wadlRepresentation;
  }

  @Override
  public RepresentationType createResponseRepresentation(MediaType mediaType) {
    RepresentationType wadlRepresentation = new RepresentationType();
    wadlRepresentation.setMediaType(mediaType.toString());
    return wadlRepresentation;
  }

  @Override
  public Param createParam(Parameter methodParameter) {
    Param wadlParameter = null;
    Annotation annotation = methodParameter.getAnnotation();
    Class<?> annotationClass = methodParameter.getAnnotation().annotationType();
    // In fact annotation may be one of from
    // MethodParameterHelper#PARAMETER_ANNOTATIONS_MAP
    if (annotationClass == PathParam.class) {
      wadlParameter = new Param();
      // attribute 'name'
      wadlParameter.setName(((PathParam) annotation).value());
      // attribute 'style'
      wadlParameter.setStyle(ParamStyle.TEMPLATE);
    } else if (annotationClass == MatrixParam.class) {
      wadlParameter = new Param();
      wadlParameter.setName(((MatrixParam) annotation).value());
      wadlParameter.setStyle(ParamStyle.MATRIX);
    } else if (annotationClass == QueryParam.class) {
      wadlParameter = new Param();
      wadlParameter.setName(((QueryParam) annotation).value());
      wadlParameter.setStyle(ParamStyle.QUERY);
    } else if (annotationClass == HeaderParam.class) {
      wadlParameter = new Param();
      wadlParameter.setName(((HeaderParam) annotation).value());
      wadlParameter.setStyle(ParamStyle.HEADER);
    }

    if (wadlParameter == null)
    // ignore this method parameter
    {
      return null;
    }

    // attribute 'repeat'
    Class<?> parameterClass = methodParameter.getParameterClass();
    if (parameterClass == List.class
        || parameterClass == Set.class
        || parameterClass == SortedSet.class) {
      wadlParameter.setRepeating(true);
    }

    // attribute 'default'
    if (methodParameter.getDefaultValue() != null) {
      wadlParameter.setDefault(methodParameter.getDefaultValue());
    }

    // attribute 'type'
    if (parameterClass.equals(Boolean.class) || parameterClass.equals(boolean.class)) {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "boolean", "xs"));
    } else if (parameterClass.equals(Byte.class) || parameterClass.equals(byte.class)) {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "byte", "xs"));
    } else if (parameterClass.equals(Short.class) || parameterClass.equals(short.class)) {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "short", "xs"));
    } else if (parameterClass.equals(Integer.class) || parameterClass.equals(int.class)) {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "integer", "xs"));
    } else if (parameterClass.equals(Long.class) || parameterClass.equals(long.class)) {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "long", "xs"));
    } else if (parameterClass.equals(Float.class) || parameterClass.equals(float.class)) {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "float", "xs"));
    } else if (parameterClass.equals(Double.class) || parameterClass.equals(double.class)) {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "double", "xs"));
    } else {
      wadlParameter.setType(new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
    }

    return wadlParameter;
  }
}
