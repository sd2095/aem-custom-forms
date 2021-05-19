package com.forms.core.core.utils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JcrUtilityService {

  public String getProperty(Node item, String property) throws RepositoryException {

    log.debug("Node name - {}", item.getName());
    log.debug("Property name - {}", property);
    String propVal = "";

    if (item.hasProperty(property))
      propVal = item.getProperty(property).getString();

    log.debug("Property Value - {}", propVal);
    return propVal;
  }
}
