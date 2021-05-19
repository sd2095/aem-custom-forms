package com.forms.core.core.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ColumnControlModel {

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @Getter
  @Setter
  public List<String> columncontrol;

  private JcrUtilityService jcrutil = new JcrUtilityService();
  
  @PostConstruct
  public void init() {

    log.info("Starting init method..");
    Node node = resource.adaptTo(Node.class);

    Map<String, Integer> map = new HashMap<>();
    map.put("c11", 2);
    map.put("c3even", 3);
    map.put("c12", 2);
    map.put("default", 1);
    map.put("c21", 2);    

    try {
      String layout = jcrutil.getProperty(node, "layout");

      log.debug("Layout val - {}",layout);
      columncontrol = new ArrayList<>();
      int inc = map.get(layout);

      log.debug("Iteration - {}", inc);

      for (int i = 1; i <= inc; i++) {
        columncontrol.add("column" + i);
      }

      for (String s : columncontrol)
        log.debug("Column control values- {}", s);
      
    } catch (Exception e) {
      log.error("Exception Caught - {}", e);
    }
  }
}
