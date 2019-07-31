package com.gol.auto.dataSource.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
//@ComponentScan({"com.gol.auto.dataSource.configuration"})
public @interface EnableAutoDataSource {
}
