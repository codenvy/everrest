package org.everrest.validation;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.everrest.core.util.Logger;

import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.ValidationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Iterator;
import java.util.List;


public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOG =
            org.everrest.core.util.Logger.getLogger(ValidationExceptionMapper.class);

    @Context
    private Configuration     config;
    @Context
    private Provider<Request> request;

    @Override
    public Response toResponse(final ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            //todo make debug.
            LOG.warn(exception.getLocalizedMessage());

            final ConstraintViolationException cve = (ConstraintViolationException)exception;
            final Response.ResponseBuilder response = Response.status(getResponseStatus(cve));

            // Entity.


            final List<Variant> variants = Variant.mediaTypes(
                    MediaType.TEXT_PLAIN_TYPE,
                    MediaType.TEXT_HTML_TYPE,
                    MediaType.APPLICATION_XML_TYPE,
                    MediaType.APPLICATION_JSON_TYPE).build();
            final Variant variant = request.get().selectVariant(variants);
            if (variant != null) {
                response.type(variant.getMediaType());
            } else {

                // default media type which will be used only when none media type from {@value variants} is in
                // accept
                // header of original request.
                // could be settable by configuration property.
                response.type(MediaType.TEXT_PLAIN_TYPE);
            }
            response.entity(
                    new GenericEntity<List<ConstraintViolationBean>>(
                            FluentIterable.from(cve.getConstraintViolations()).transform(
                                    new Function<ConstraintViolation<?>, ConstraintViolationBean>() {

                                        @Override
                                        public ConstraintViolationBean apply(ConstraintViolation<?> input) {
                                            return ConstraintViolationBean.fromConstraintViolation(input);
                                        }
                                    }).toList()

                            ,
                            new GenericType<List<ConstraintViolationBean>>() {
                            }.getType()
                    )
                           );


            return response.build();
        } else {
            //todo make debug.
            LOG.warn(exception.getLocalizedMessage());
            return Response.serverError().entity(exception.getMessage()).build();
        }
    }


    /**
     * Determine the response status (400 or 500) from the given BV exception.
     *
     * @param violation
     *         BV exception.
     * @return response status (400 or 500).
     */
    public static Response.Status getResponseStatus(final ConstraintViolationException violation) {
        final Iterator<ConstraintViolation<?>> iterator = violation.getConstraintViolations().iterator();

        if (iterator.hasNext()) {
            for (final Path.Node node : iterator.next().getPropertyPath()) {
                final ElementKind kind = node.getKind();

                if (ElementKind.RETURN_VALUE.equals(kind)) {
                    return Response.Status.INTERNAL_SERVER_ERROR;
                }
            }
        }

        return Response.Status.BAD_REQUEST;
    }

}
