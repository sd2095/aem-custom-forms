package com.forms.core.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import lombok.extern.slf4j.Slf4j;


@Component(service = Servlet.class)
@SlingServletPaths("/bin/forms/constraintdatasource")
@Slf4j

public class ConstraintDatasourceServlet extends SlingAllMethodsServlet {

  private static final long serialVersionUID = -653109785622106743L;
  
  @ObjectClassDefinition(name = "Form Authoring Datasource Config for retrieving constraints",
      description = "List of form component paths")
  public @interface Config {    

    @AttributeDefinition(name = "Form Components AEM Paths", description="Property value should be like - form component path | name of parsys in form container")
    String[] componentPaths() default {"/apps/forms/components|form_container"};
  }
  
  private Map<String,String> componentNodeMap;

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());
    ResourceResolver resolver = request.getResourceResolver();
    String requestUrl = request.getRequestURI();
    log.debug("Request path - {}", requestUrl);
    boolean pathConfigured = checkPathConfiguration(requestUrl);

    try {

      if (pathConfigured) {

        String subStr = requestUrl.substring(requestUrl.lastIndexOf("_cq_dialog.html")+15);
        log.debug("Substring path - {}",subStr);
        Resource resource = resolver.getResource(subStr);
        Node node = resource.adaptTo(Node.class);

        if (node.hasProperty("rulepath")) {

          String rulePath = node.getProperty("rulepath").getString();
          log.debug("Rule path {}",rulePath);
          Resource rulesResource = resolver.getResource(rulePath + "/jcr:content/list");
          Node validationRules = rulesResource.adaptTo(Node.class);
          List<Resource> dropdownConstraintsList = new ArrayList<>();
          ValueMap vm = null;
          NodeIterator nodeItr = validationRules.getNodes();

          while (nodeItr.hasNext()) {

            vm = new ValueMapDecorator(new HashMap<String, Object>());
            Node items = nodeItr.nextNode();
            String value = items.getProperty("validation_name").getString();

            vm.put("value", value);
            vm.put("text", value);

            dropdownConstraintsList
                .add(new ValueMapResource(resolver, new ResourceMetadata(), "nt:unstructured", vm));

            DataSource ds = new SimpleDataSource(dropdownConstraintsList.iterator());
            request.setAttribute(DataSource.class.getName(), ds);

          }
        } else {
          log.debug("Rule Path Not configured");
        }
      } else {
        log.debug("Form container not present");
      }
    } catch (Exception e) {
      log.error("Exception Caught - {}", e);
    }

  }

  private boolean checkPathConfiguration(String requestUrl) {

    boolean flag = false;
    
    for(Entry<String, String> set : componentNodeMap.entrySet()) {
      String path = set.getKey();
      log.debug("Path in hashed map - {}",path);
      if(requestUrl.contains(path)) {
        flag = true;
      }
    }
    return flag;
  }
  
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    doPost(request, response);
  }
  
  @Activate
  protected void activate(Config config) {
    
    componentNodeMap = new HashMap<>();
    
    List<String> componentPathsList = Arrays.asList(config.componentPaths());
    for (String path : componentPathsList) {
      componentNodeMap.put(path.split("\\|")[0], path.split("\\|")[1]);
      log.debug("Map entry - {}",componentNodeMap.size());
    }    
  }

}
