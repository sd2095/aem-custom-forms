package com.forms.core.core.models;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import com.adobe.xfa.ut.StringUtils;
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
public class FormRulesModel {

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @ValueMapValue
  @Getter
  @Setter
  private String constraints = "";


  @PostConstruct
  public void init() {

    try {
      log.debug("Starting init method");
      Node node = resource.adaptTo(Node.class);
      String rulepath = node.hasProperty("rulepath") ? node.getProperty("rulepath").getString() : "";
      log.debug("Current Path of main form: {}, validation rule path: {}", node.getPath(),
          rulepath);
      if (!StringUtils.isEmpty(rulepath)) {

        NodeIterator nodeItr = resourceResolver.getResource(rulepath + "/jcr:content/list")
            .adaptTo(Node.class).getNodes();
        StringBuilder res = new StringBuilder("");

        while (nodeItr.hasNext()) {

          Node currentNode = nodeItr.nextNode();
          boolean defaultMethod = currentNode.hasProperty("default_method");

          if (!defaultMethod) {

            String regex = currentNode.getProperty("validation_regex").getString();
            String name = currentNode.getProperty("validation_name").getString();
            String customMsg = currentNode.getProperty("validation_message").getString();

            StringBuilder str = new StringBuilder("$.validator.addMethod('");
            str.append(name).append("', function (value, element) {").append("\n");
            str.append("   return this.optional(element) || value.match(new RegExp('");
            str.append(regex).append("'));").append("\n");
            str.append("}, ").append("'").append(customMsg).append("');").append("\n");

            res.append(str);
          }
        }
        constraints = res.toString();
        log.debug("Constraint : {}", constraints);
      } else {
        log.debug("There are no validation in the form");
      }
    } catch (Exception ex) {
      log.error("Exception Caught : {}", ex);
    }
  }
}
