package com.forms.core.core.models;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import com.forms.core.core.utils.JcrUtilityService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = {SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Slf4j
public class DropdownScript {

  private static final String JQCST = "\";\n";

  private static final String COMPARE_TRUE = "\"===\"true\";\n";

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;


  @Getter
  @Setter
  public String script = "";

  private JcrUtilityService jcrutil = new JcrUtilityService();

  @PostConstruct
  public void init() {

    try {

      log.debug("Starting dropdown script");
      Node dropdownNode = resource.adaptTo(Node.class);
      
      log.debug("Node name - {}, node path - {}", dropdownNode.getName(), dropdownNode.getPath());
      
      StringBuilder sb = new StringBuilder();
      String nodeName = dropdownNode.getName();
      String nodeId = jcrutil.getProperty(dropdownNode, "fieldId");
      String parentId = jcrutil.getProperty(dropdownNode, "valueId");
      String multiSelect = jcrutil.getProperty(dropdownNode, "multipleSelection");
      String groupedData = jcrutil.getProperty(dropdownNode, "grouping");
      String placeHolder = jcrutil.getProperty(dropdownNode, "placeholder");
      String typeAhead = jcrutil.getProperty(dropdownNode, "enableTypeAhead");
      String useAsLink = jcrutil.getProperty(dropdownNode, "useAsLink");
      String maxSelectionLength = jcrutil.getProperty(dropdownNode, "maxSelectionLength");
            
      sb.append("var ").append(nodeId).append(" = new Object();\n");
      sb.append(nodeId).append(".fieldId = \"").append(nodeId).append(JQCST);
      sb.append(nodeId).append(".nodeName = \"").append(nodeName).append(JQCST);
      sb.append(nodeId).append(".parentId = \"").append(parentId).append(JQCST);
      sb.append(nodeId).append(".multipleSelection = \"").append(multiSelect).append(COMPARE_TRUE);
      sb.append(nodeId).append(".grouping = \"").append(groupedData).append(COMPARE_TRUE);
      sb.append(nodeId).append(".placeholder = \"").append(placeHolder).append(JQCST);
      sb.append(nodeId).append(".enableTypeAhead = \"").append(typeAhead).append(COMPARE_TRUE);
      sb.append(nodeId).append(".useAsLink = \"").append(useAsLink).append(COMPARE_TRUE);
      sb.append(nodeId).append(".maxSelectionLength = \"").append(maxSelectionLength).append(JQCST);

      sb.append("var ").append(nodeId+"useAsLabel").append(" = \"" +jcrutil.getProperty(dropdownNode, "useAsLabel")).append(COMPARE_TRUE);
      
      sb.append("ftsDropdownInit(").append(nodeId).append(");\n");             
      script = sb.toString();
      log.debug("Script string - {}",script);
    } catch (Exception e) {

      log.error("Exception caught - {}",e);
    }
  }
}
