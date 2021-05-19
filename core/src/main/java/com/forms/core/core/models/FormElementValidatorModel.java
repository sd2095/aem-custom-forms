package com.forms.core.core.models;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = {SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Slf4j
public class FormElementValidatorModel {

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @ValueMapValue
  @Getter
  @Setter
  private boolean checkFormContainer = false;
  
  @ValueMapValue
  @Getter
  @Setter
  private String message = "<h3 style='color:red'>Container not supported. Please use forms container</h3>";

  @PostConstruct
  public void init() {

    log.debug("Starting init");

    Node node = resource.adaptTo(Node.class);

    try {

      if (checkContainer(node)) {
        log.debug("Form container found, component path- {}",node.getPath());
        checkFormContainer = true;
      }
    } catch (Exception e) {

      log.error("Exception caught: {}", e);
    }

  }

  private boolean checkContainer(Node node) throws RepositoryException {
    
    if (node.isNodeType("cq:Page")) {

      log.debug("Form Container not found, node is of type cq:Page");
      return false;
      
    } else if (node.hasProperty("sling:resourceType") && node.getProperty("sling:resourceType").getString()
        .contains("forms/components/form-container")) {

      log.debug("Node name: {}, node path: {}", node.getName(), node.getPath());
      
      return true;
      
    } else {

      log.debug("Recursive call to get form container as parent node");
      return checkContainer(node.getParent());
    }    

  }

}
