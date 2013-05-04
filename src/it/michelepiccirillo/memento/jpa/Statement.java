package it.michelepiccirillo.memento.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Statement {
	String value();
}
