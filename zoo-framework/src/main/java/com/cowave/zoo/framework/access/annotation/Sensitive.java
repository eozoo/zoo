package com.cowave.zoo.framework.access.annotation;

import java.lang.annotation.*;

/**
 *
 * @author shanhuiming
 *
 */
@Inherited
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

}
