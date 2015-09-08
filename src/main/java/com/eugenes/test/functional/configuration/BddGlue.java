package com.eugenes.test.functional.configuration;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

@ContextHierarchy({@ContextConfiguration("classpath:/cucumber.xml")})
// @DirtiesContext
@Component
public abstract class BddGlue {

}
