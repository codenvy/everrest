package org.everrest.validation;

import org.everrest.core.impl.method.MethodInvokerDecorator;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.method.MethodInvoker;

/**
 * @author Sergii Kabashniuk
 */
public class ValidationMethodInvokerDecoratorFactory  implements MethodInvokerDecoratorFactory {
    @Override
    public MethodInvokerDecorator makeDecorator(MethodInvoker invoker) {
        return new ValidationMethodInvokerDecorator(invoker);
    }
}
