package com.domenic;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Domenic
 * @Classname EnabledIfImageMagickAvailable
 * @Description TODO
 * @Date 3/24/2023 9:27 PM
 * @Created by Domenic
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(EnabledIfImageMagickIsAvailableCondition.class)
public @interface EnabledIfImageMagickIsAvailable {
}
