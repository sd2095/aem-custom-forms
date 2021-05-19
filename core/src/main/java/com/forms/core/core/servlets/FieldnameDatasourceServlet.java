package com.forms.core.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import lombok.extern.slf4j.Slf4j;

@Component(service = Servlet.class)
@SlingServletResourceTypes(resourceTypes = "/bin/forms/fieldnamedatasource")
@Designate(ocd = FieldnameDatasourceServlet.Config.class)
@Slf4j

public class FieldnameDatasourceServlet extends SlingAllMethodsServlet {

  private static final long serialVersionUID = -653109785622106743L;

  @ObjectClassDefinition(name = "Form Authoring DataSource Config for field names",
      description = "List of resource types and form component path to be maintained here")
  public @interface Config {

    @AttributeDefinition(name = "ResourceType Lists",
        description = "This list should be maintained for populating validations of the fields configured")
    String[] resourceTypes() default {"forms/components/textfield%"};

    @AttributeDefinition(name = "Form Components AEM Paths",
        description = "Property value should be like - form component path | name of parsys in form container")
    String[] componentPaths() default {"/apps/forms/components|form_container"};
  }

  private List<String> resourceTypeList;
  private List<String> newList;
  @Reference
  private transient QueryBuilder builder;
  private Map<String, String> componentNodeMap;

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

        String subStr = requestUrl.substring(requestUrl.lastIndexOf("_cq_dialog.html") + 15);
        newList = new ArrayList<>();
        filterResourceTypes(request);
        log.debug("Substring path - {}", subStr);
        Resource contentResource = resolver.getResource(subStr);

        Node node = contentResource.adaptTo(Node.class);
        List<SearchResult> resultSet = new ArrayList<>();
        buildResultSet(subStr, resolver, resultSet);
        List<Resource> dropdownConstraintsList = new ArrayList<>();

        if ((node.hasNode("formContainer")) && !resultSet.isEmpty()) {
          setDatasourceObject(request, resolver, resultSet, dropdownConstraintsList);

        } else {
          log.debug("No components found inside form container");
        }
      }
    } catch (Exception e) {
      log.error("Exception Caught in FieldName datasource :{} ", e);
    }
  }

  private void filterResourceTypes(SlingHttpServletRequest request) {

    Resource dialogResource = request.getResource();

    String retrieveVal =
        dialogResource.getChild("datasource").getValueMap().get("retrieve", String.class);
    log.debug("Prop val - {}", retrieveVal);

    for (String s : resourceTypeList) {
      newList.add(s);
    }

    if (!StringUtils.isEmpty(retrieveVal)) {
      if (retrieveVal.equalsIgnoreCase("formElements")) {
        
        newList.remove("forms/components/button%");        

      } else if (retrieveVal.equalsIgnoreCase("modals")) {
        for (String str : resourceTypeList) {
          if (!str.contains("modal")) {
            newList.remove(str);
          }
        }
      } else if (retrieveVal.equalsIgnoreCase("paramElements")) {

        newList = new ArrayList<>();
        newList.add("forms/components/radio%");
        newList.add("forms/components/textfield%");
        newList.add("forms/components/dropdown%");
        newList.add("forms/components/textarea%");
      }
    }
  }


  private void setDatasourceObject(SlingHttpServletRequest request, ResourceResolver resolver,
      List<SearchResult> resultSet, List<Resource> dropdownConstraintsList)
      throws RepositoryException {

    ValueMap vm = null;
    for (SearchResult res : resultSet) {
      for (Hit hit : res.getHits()) {

        vm = new ValueMapDecorator(new HashMap<String, Object>());
        Node n = hit.getNode();
        String value = n.hasProperty("fieldId") ? n.getProperty("fieldId").getString()
            : "";

        vm.put("value", value);
        vm.put("text", value);

        dropdownConstraintsList
            .add(new ValueMapResource(resolver, new ResourceMetadata(), "nt:unstructured", vm));

        DataSource ds = new SimpleDataSource(dropdownConstraintsList.iterator());
        request.setAttribute(DataSource.class.getName(), ds);

      }
    }
  }

  private boolean checkPathConfiguration(String requestUrl) {

    boolean flag = false;

    for (Entry<String, String> set : componentNodeMap.entrySet()) {
      String path = set.getKey();
      if (requestUrl.contains(path)) {
        flag = true;
      }
    }
    return flag;
  }

  private void buildResultSet(String subStr, ResourceResolver resourceResolver,
      List<SearchResult> resultSet) {
    Map<String, String> map;
    for (String rsrcTyp : newList) {
      map = new HashMap<>();
      map.put("path", subStr);
      map.put("type", "nt:unstructured");
      map.put("1_property", "sling:resourceType");
      map.put("1_property.value", rsrcTyp);
      map.put("1_property.operation", "like");
      Session session = resourceResolver.adaptTo(Session.class);
      if (session != null) {
        Query query = builder.createQuery(PredicateGroup.create(map), session);
        SearchResult result = query.getResult();
        resultSet.add(result);
      } else {
        log.debug("Session is null");
      }
    }
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    doPost(request, response);
  }

  @Activate
  protected void activate(Config config) {

    componentNodeMap = new HashMap<>();
    resourceTypeList = Arrays.asList(config.resourceTypes());
    List<String> componentPathsList = Arrays.asList(config.componentPaths());
    for (String path : componentPathsList) {
      componentNodeMap.put(path.split("\\|")[0], path.split("\\|")[1]);
    }
    log.info("Retrieving list of resourceTypes from configuration of size- {}",
        resourceTypeList.size());
  }

}
