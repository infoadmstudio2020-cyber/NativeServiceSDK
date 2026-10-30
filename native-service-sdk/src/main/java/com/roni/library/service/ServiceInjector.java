package com.roni.library.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Dependency injector utility for automatically wiring services from {@link ServiceRegistry}.
 */
public final class ServiceInjector {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InjectService {
        Class<? extends IService> value() default IService.class;
        String name() default "";
    }

    private ServiceInjector() {
    }

    /**
     * Injects registered services into annotated fields of the target object.
     *
     * @param target object whose fields should be injected
     */
    public static void inject(Object target) {
        if (target == null) {
            return;
        }
        ServiceRegistry registry = ServiceRegistry.getInstance();
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(InjectService.class)) {
                    InjectService annotation = field.getAnnotation(InjectService.class);
                    Object service = null;
                    if (annotation != null && !annotation.name().isEmpty()) {
                        service = registry.getServiceByName(annotation.name());
                    } else if (annotation != null && annotation.value() != IService.class) {
                        service = registry.getService(annotation.value());
                    } else if (IService.class.isAssignableFrom(field.getType())) {
                        @SuppressWarnings("unchecked")
                        Class<? extends IService> serviceClass = (Class<? extends IService>) field.getType();
                        service = registry.getService(serviceClass);
                    }

                    if (service != null) {
                        boolean accessible = field.isAccessible();
                        try {
                            field.setAccessible(true);
                            field.set(target, service);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Could not inject field: " + field.getName(), e);
                        } finally {
                            field.setAccessible(accessible);
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
