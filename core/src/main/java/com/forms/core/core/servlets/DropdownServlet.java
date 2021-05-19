package com.forms.core.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import com.forms.core.core.bean.GenericDropdown;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/forms/dropddownservlet")
@Slf4j

public class DropdownServlet extends SlingAllMethodsServlet {

  private transient Gson gson = new Gson();
  private static final long serialVersionUID = -1770304341655004912L;
  private static final String VALUE = "value";
  private static final String TITLE = "title";

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {
    
    GenericDropdown city;
    ArrayList<GenericDropdown> cities = new ArrayList<>();
    try {
 
      Iterator<Resource> resItr = resolveResource(request);                                       
            
      while (resItr!= null && resItr.hasNext()) {
        ValueMap properties = resItr.next().adaptTo(ValueMap.class);
        if (properties.get("parentValue") != null) {
          if (!StringUtils.isEmpty(properties.get(TITLE).toString())
              && !StringUtils.isEmpty(properties.get(VALUE).toString())) {
            city = new GenericDropdown(properties.get("parentValue").toString(),
                properties.get(TITLE).toString(), properties.get(VALUE).toString());
            cities.add(city);
          }
        } else {
          String title;
          if (properties.get(TITLE) != null) {
            title = properties.get(TITLE).toString();
            city = new GenericDropdown(properties.get(VALUE).toString(), title);
            cities.add(city);
          } else if (properties.get("jcr:title") != null) {
            title = properties.get("jcr:title").toString();
            city = new GenericDropdown(properties.get(VALUE).toString(), title);
            cities.add(city);
          } else {
            log.debug("jcr:title or title property not present in the node");
          }
        }
      }

    } catch (Exception e) {
      city = new GenericDropdown("Error", "Exception Caught - "+e.getMessage());
      cities.add(city);
      log.error("Exception caught in servlet : {}", e);
    }

    String employeeJsonString = this.gson.toJson(cities);
    
    
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(employeeJsonString);
    response.getWriter().flush();

  }

  private Iterator<Resource> resolveResource(SlingHttpServletRequest request) {

    String contentPath = request.getParameter("contentPath");
    log.debug("Content Path : {}", contentPath);
        
    ResourceResolver resourceResolver = request.getResourceResolver();
    
    if(resourceResolver.getResource(contentPath + "/jcr:content/list") != null) {
      
      return resourceResolver.getResource(contentPath + "/jcr:content/list").listChildren();
    }
    else {
      
      return resourceResolver.getResource(contentPath).getChild("options").listChildren();
    }        
  }

}
