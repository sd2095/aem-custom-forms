package com.forms.core.core.models;

import static org.apache.sling.models.annotations.DefaultInjectionStrategy.OPTIONAL;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.forms.core.core.utils.JcrUtilityService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = {Resource.class}, defaultInjectionStrategy = OPTIONAL)
@Slf4j

public class FormConfig {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @ValueMapValue
  @Getter
  @Setter
  private String constraints = "";

  @Inject
  private QueryBuilder builder;

  private JcrUtilityService jcrutil = new JcrUtilityService();
  
  @PostConstruct
  public void init() {

    JSONObject config = new JSONObject();
    JSONObject rules = new JSONObject();
    JSONObject messages = new JSONObject();
    try {

      addGlobalConstraints(rules, messages);
      addComponentConstraints(rules, messages);
      config.put("rules", rules);
      config.put("messages", messages);
      constraints = config.toString();

    } catch (JSONException e) {

      log.error("Exception caught in init method: {}", e);
    }

  }

  private void addGlobalConstraints(JSONObject rules, JSONObject messages) {
    try {

      Node node = resource.adaptTo(Node.class);

      log.debug("Node path : {}", node.getPath());

      if (node.hasNode("global_constraint_parent")) {

        Node validationNode = node.getNode("global_constraint_parent");

        NodeIterator nodeItr = validationNode.getNodes();

        while (nodeItr.hasNext()) {

          JSONObject ruleObj = new JSONObject();
          JSONObject messageObj = new JSONObject();
          Node component = nodeItr.nextNode();

          String fieldId = jcrutil.getProperty(component, "global_constraint_fieldname");
          String methodname = jcrutil.getProperty(component, "global_constraint_fieldconstraint");
          String message = jcrutil.getProperty(component, "global_constraint_message");

          log.debug("FieldId : {} , Method name : {} , Message : {}", fieldId, methodname, message);

          addRules(rules, messages, ruleObj, messageObj, fieldId, methodname, message);
        }
      } else {
        log.debug("No global validation declared ");
      }
    } catch (Exception e) {
      logger.error("Exception caught in addConstraint method : {}", e);
    }
  }

  private void addRules(JSONObject rules, JSONObject messages, JSONObject ruleObj,
      JSONObject messageObj, String fieldId, String methodname, String message)
      throws JSONException {
    if (rules.has(fieldId)) {

      rules.getJSONObject(fieldId).put(methodname, true);
      messages.getJSONObject(fieldId).put(methodname, message);
    } else {

      ruleObj.put(methodname, true);
      messageObj.put(methodname, message);
      rules.put(fieldId, ruleObj);
      messages.put(fieldId, messageObj);
    }
  }

  private void addComponentConstraints(JSONObject rules, JSONObject messages) {

    try {

      Node node = resource.adaptTo(Node.class);
      log.debug("Node path : {}", node.getPath());
      SearchResult sr = buildResultSet(node.getPath());

      if (sr != null) {
        for (Hit hit : sr.getHits()) {

          Node n = hit.getNode();
          JSONObject ruleObj = new JSONObject();
          JSONObject messageObj = new JSONObject();

          String fieldId = jcrutil.getProperty(n.getParent().getParent(), "intfieldId");
          String message = jcrutil.getProperty(n, "validation_msg");
          String methodname = jcrutil.getProperty(n, "constraint");

          addRules(rules, messages, ruleObj, messageObj, fieldId, methodname, message);

        }
      }

    } catch (Exception e) {

      log.error("Exception caught in component constraints method - {}", e);
    }

  }

  private SearchResult buildResultSet(String subStr) {
    Map<String, String> map;

    map = new HashMap<>();
    map.put("path", subStr);
    map.put("type", "nt:unstructured");
    map.put("1_property", "constraint");
    map.put("1_property.operation", "exists");

    Session session = resourceResolver.adaptTo(Session.class);
    if (session != null) {
      Query query = builder.createQuery(PredicateGroup.create(map), session);
      return query.getResult();

    } else {
      log.debug("Session is null");
      return null;
    }

  }
}
