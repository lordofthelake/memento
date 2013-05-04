package it.michelepiccirillo.memento;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Delegate {
	Class<?> value();
	String method() default "";
}
