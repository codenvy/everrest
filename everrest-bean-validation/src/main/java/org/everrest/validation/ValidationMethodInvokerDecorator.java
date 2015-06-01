package org.everrest.validation;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.method.DefaultMethodInvoker;
import org.everrest.core.impl.method.MethodInvokerDecorator;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericMethodResource;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sergii Kabashniuk
 */
public class ValidationMethodInvokerDecorator extends MethodInvokerDecorator {
    private final Validator        validator;
    private final Configuration<?> configuration;

    /**
     * @param decoratedInvoker
     *         decorated MethodInvoker
     */
    ValidationMethodInvokerDecorator(MethodInvoker decoratedInvoker) {
        super(decoratedInvoker);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        configuration = Validation.byDefaultProvider().configure();
    }

    @Override
    public Object invokeMethod(Object resource, GenericMethodResource resourceMethod,
                               ApplicationContext context) {


        final Set<ConstraintViolation<Object>> constraintViolations = new HashSet<>();
        final BeanDescriptor beanDescriptor = validator.getConstraintsForClass(resource.getClass());

        // Resource validation.
        if (beanDescriptor.isBeanConstrained()) {
            constraintViolations.addAll(validator.validate(resource));
        }

        if (resourceMethod != null
            && configuration.getBootstrapConfiguration().isExecutableValidationEnabled()) {
            final Method handlingMethod = resourceMethod.getMethod();

            // Resource method validation - input parameters.
            final MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod(handlingMethod.getName(),
                                                                                             handlingMethod
                                                                                                     .getParameterTypes());

            if (methodDescriptor != null
                && methodDescriptor.hasConstrainedParameters()) {
                constraintViolations.addAll(validator.forExecutables().validateParameters(resource, handlingMethod,
                                                                                          DefaultMethodInvoker
                                                                                                  .makeMethodParameters(
                                                                                                          resourceMethod,
                                                                                                          context)));
            }
        }

        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
        Object result = super.invokeMethod(resource, resourceMethod, context);
        if (configuration.getBootstrapConfiguration().isExecutableValidationEnabled()) {
            final Method handlingMethod = resourceMethod.getMethod();


            final MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod(handlingMethod.getName(),
                                                                                             handlingMethod
                                                                                                     .getParameterTypes());


            if (methodDescriptor != null && methodDescriptor.hasConstrainedReturnValue()) {
                constraintViolations
                        .addAll(validator.forExecutables().validateReturnValue(resource, handlingMethod, result));

                if (result instanceof Response) {
                    constraintViolations.addAll(validator.forExecutables().validateReturnValue(resource, handlingMethod,
                                                                                               ((Response)result)
                                                                                                       .getEntity()));
                }
            }

            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
        return result;
    }


}
