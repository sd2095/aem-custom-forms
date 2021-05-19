package com.forms.core.core.models;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.forms.core.core.utils.JcrUtilityService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = {SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Slf4j
public class ScriptGenerator {

  private JcrUtilityService jcrutil = new JcrUtilityService();
  private StringBuilder globalFunctionScript = new StringBuilder("");
  private StringBuilder functionBuilder;

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @Inject
  private QueryBuilder builder;

  @Inject
  SlingHttpServletRequest request;

  @ValueMapValue
  @Getter
  @Setter
  public String script = "";
  
  
  private static final String AJAXENDPOINT = "ajaxendpoint";
  private static final String CONTENTPATH = "contentPath";
  private static final String OPTIONSOURCE = "optionsSource";
  private static final String AJAXSTART = "$.ajax({\n";
  private static final String AJAXURL = "    url: \"";
  private static final String NEXTLINE = "\", \n";
  private static final String AJAXTYPE = "	 type: \"GET\",\n";
  private static final String AJAXSUCCESS = "    success: function(result){\n";
  private static final String NODE_WRAPPER = "valueId";
  private static final String JQCST = "\t$('#";
  private static final String FIELD_ID = "fieldId";
  private static final String PARENT_DROP_DOWN_ID = "parentDropDownID";
  private static final String IS_HIDDEN = "hideDropdown";
  
  
  private String nodeName;  
  private String nodeId;
  private String nodeWrapper;
  private String parentId;
  private String contentpth;
  private String optSrc;
  private String isHidden;
  
  @PostConstruct
  public void init() {

    try {
      log.info("Starting init method..");      

      
      Map<String, String> map = new HashMap<>();
      map.put("path", resource.getPath());
      map.put("type", "nt:unstructured");
      map.put("1_property", "sling:resourceType");
      map.put("1_property.value", "forms/components/dropdown%");
      map.put("1_property.operation", "like");
      Session session = resourceResolver.adaptTo(Session.class);
      Query query = builder.createQuery(PredicateGroup.create(map), session);
      SearchResult result = query.getResult();

      if (!result.getHits().isEmpty()) {

        for (Hit hit : result.getHits()) {
          Node cNode = hit.getNode();
          initializeNodeProps(cNode);
          functionBuilder = new StringBuilder("\nfunction "+nodeName+nodeId+ "() {");
          if (cNode.hasProperty(PARENT_DROP_DOWN_ID)) {
            processConditionalDropDown(cNode);
          }
          processScript(cNode);
          functionBuilder.append("}\n");  
          functionBuilder.append(nodeName+nodeId+"();\n");
          globalFunctionScript.append(functionBuilder);
          
        }
      } else {
        log.debug("Container doesn't have dropdown component");
      }
      script = globalFunctionScript.toString();

    } catch (Exception ex) {

      log.error("Exception Caught in init method of Script Generator : {}", ex);
    }
  }

  private void initializeNodeProps(Node node) throws RepositoryException {

    nodeName = node.getName();
    nodeId = jcrutil.getProperty(node, FIELD_ID);
    nodeWrapper = jcrutil.getProperty(node, NODE_WRAPPER);
    parentId = jcrutil.getProperty(node, PARENT_DROP_DOWN_ID);
    contentpth = jcrutil.getProperty(node, CONTENTPATH);
    optSrc = jcrutil.getProperty(node, OPTIONSOURCE);
    isHidden = jcrutil.getProperty(node, IS_HIDDEN);
    
  }

  private void processScript(Node cNode)
      throws RepositoryException {
    try {
      String ajaxendpoint = getEndPointUrl(cNode);      
      
      log.debug("This is a independent dropdown : {}", cNode.getName());
      if (optSrc.equalsIgnoreCase("genericlist")) {

        log.debug("Fethcing data from generic list");
        functionBuilder
            .append(buildDropdown(ajaxendpoint, contentpth));

      } else if (optSrc.equalsIgnoreCase("static")) {

        log.debug("Path of the component - {}", cNode.getPath());
        functionBuilder
            .append(buildDropdown(ajaxendpoint, cNode.getPath()));

      } else if (optSrc.equalsIgnoreCase("server")) {

        log.debug("Fetching data from a servlet");
        functionBuilder.append(buildDropdown(ajaxendpoint));

      } else {
        log.debug("Proper source not configured");
      }

    } catch (PathNotFoundException pathex) {
      log.error("Path not found in static dropdown: {}", pathex);
    } catch (Exception ex) {
      log.error("Exception caught in processing static dropdown: {}", ex);
    }
  }

  private String getEndPointUrl(Node node) throws RepositoryException {

    String url = jcrutil.getProperty(node, AJAXENDPOINT);
    if (jcrutil.getProperty(node, "multipleSource").equalsIgnoreCase("true") && checkAuthCookie()
        && !StringUtils.isEmpty(jcrutil.getProperty(node, "authajaxendpoint"))) {

      url = jcrutil.getProperty(node, "authajaxendpoint");

    }
    log.debug("resolved ajax url - {}", url);
    return url;
  }

  private boolean checkAuthCookie() {

    Cookie[] cookie = request.getCookies();
    boolean auth = false;

    for (Cookie c : cookie) {
      log.debug("Cookie name - {}, val - {}", c.getName(), c.getValue());

      if (c.getName().equalsIgnoreCase("tokenId") || c.getName().equalsIgnoreCase("login-token")) {
        auth = true;
        break;
      }
    }
    return auth;
  }

  private void processConditionalDropDown(Node cNode) throws RepositoryException {
    try {

      log.debug("This is a conditional dropdown : {}", cNode.getName());            
      generateOnChangeScript();      
    } catch (PathNotFoundException pathex) {
      log.error("Path Not Found : {}", pathex);
    } catch (Exception ex) {
      log.error("Exception Caught in processing conditional dropdown: {}", ex);
    }
  }


  private void generateOnChangeScript() {

    StringBuilder sb = new StringBuilder();
    sb.append("$('#").append(parentId).append("').on('change.select2', function() { \n");
    
    if(optSrc.equalsIgnoreCase("server")) {
      sb.append(nodeName+nodeId+"();\n");
    }
    else {
      sb.append("\tdropdownFilter(this.value, \"").append(nodeId).append("\");\n");
    }
                
    sb.append(JQCST + nodeId + "').val('').trigger('change');\n");
                
    if (isHidden.equalsIgnoreCase("true")) {
      sb.append("if($('#" + nodeId + "').children().length > 1) {");
      sb.append(JQCST + nodeWrapper + "').removeClass('fts-hidden');\n}else{");
      sb.append(JQCST + nodeWrapper + "').addClass('fts-hidden');\n}");
    }

    sb.append("\n});");

    globalFunctionScript.append(sb);
  }

  // this is for generic list
  private String buildDropdown(String ajaxendpoint, String contentPath) {

    String res = AJAXSTART + AJAXURL + ajaxendpoint + NEXTLINE + AJAXTYPE
        + "    data: {contentPath:\"" + contentPath + "\"},\n" + AJAXSUCCESS
        + "        jsonData = {\"data\":JSON.parse(result)};\r\n";
    
    res += nodeId+ ".ajaxSuccess(jsonData);\r\n";
    if (!StringUtils.isEmpty(parentId)) {

      res += "$('#" + nodeId + "').html('<option></option>');\n";
    }

    return res + "    }\r\n    });";
  }

  // this is for manual authoring and server call
  private String buildDropdown(String ajaxendpoint) {

    String res = AJAXSTART + AJAXURL + ajaxendpoint + NEXTLINE + AJAXTYPE + AJAXSUCCESS
        + "        jsonData = {\"data\":JSON.parse(result)};\r\n";
    
    res += nodeId+ ".ajaxSuccess(jsonData);\r\n";
   
    if (!StringUtils.isEmpty(parentId)) {
      res += "$('#" + nodeId + "').html('<option></option>');\n";
    }
    
    return res + "    }\r\n" + "    });";
  }  

}
