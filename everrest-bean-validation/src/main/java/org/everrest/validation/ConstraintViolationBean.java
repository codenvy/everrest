package org.everrest.validation;

import javax.validation.ConstraintViolation;
import java.util.Arrays;

/**
 * @author Sergii Kabashniuk
 */
public class ConstraintViolationBean {

    private String message;

    private String messageTemplate;

    private String path;

    private String invalidValue;


    public ConstraintViolationBean() {
    }

    public ConstraintViolationBean(String message, String messageTemplate, String path,
                                   String invalidValue) {
        this.message = message;
        this.messageTemplate = messageTemplate;
        this.path = path;
        this.invalidValue = invalidValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }


    public static ConstraintViolationBean fromConstraintViolation(
            ConstraintViolation constraintViolation) {
        return new ConstraintViolationBean(constraintViolation.getMessage(),
                                           constraintViolation.getMessageTemplate(),
                                           getViolationPath(constraintViolation),
                                           getViolationInvalidValue(constraintViolation.getInvalidValue()));
    }

    /**
     * Provide a string value of (invalid) value that caused the exception.
     *
     * @param invalidValue
     *         invalid value causing BV exception.
     * @return string value of given object or {@code null}.
     */
    private static String getViolationInvalidValue(final Object invalidValue) {
        if (invalidValue == null) {
            return null;
        }

        if (invalidValue.getClass().isArray()) {
            if (invalidValue instanceof Object[]) {
                return Arrays.toString((Object[])invalidValue);
            } else if (invalidValue instanceof boolean[]) {
                return Arrays.toString((boolean[])invalidValue);
            } else if (invalidValue instanceof byte[]) {
                return Arrays.toString((byte[])invalidValue);
            } else if (invalidValue instanceof char[]) {
                return Arrays.toString((char[])invalidValue);
            } else if (invalidValue instanceof double[]) {
                return Arrays.toString((double[])invalidValue);
            } else if (invalidValue instanceof float[]) {
                return Arrays.toString((float[])invalidValue);
            } else if (invalidValue instanceof int[]) {
                return Arrays.toString((int[])invalidValue);
            } else if (invalidValue instanceof long[]) {
                return Arrays.toString((long[])invalidValue);
            } else if (invalidValue instanceof short[]) {
                return Arrays.toString((short[])invalidValue);
            }
        }

        return invalidValue.toString();
    }

    /**
     * Get a path to a field causing constraint violations.
     *
     * @param violation
     *         constraint violation.
     * @return path to a property that caused constraint violations.
     */
    private static String getViolationPath(final ConstraintViolation violation) {
        final String rootBeanName = violation.getRootBean().getClass().getSimpleName();
        final String propertyPath = violation.getPropertyPath().toString();

        return rootBeanName + (!"".equals(propertyPath) ? '.' + propertyPath : "");
    }

}
