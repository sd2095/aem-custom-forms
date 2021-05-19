package com.forms.core.core.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.apache.commons.lang3.StringUtils;
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
public class MultifieldModel {

  private static final String LOGGER_CONSTANT = "Mapped value : {}";

  private static final String OPTIONS = "options";

  private static final String GROUPEDOPTIONS = "groupedoptions";

  private static final String VALUE = "value";

  private static final String TITLE = "title";

  private static final String GROUP_NAME = "grpName";

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @Getter
  @Setter
  public Map<String, LinkedHashMap<String, String>> groupDataMap;

  @Getter
  @Setter
  public Map<String, String> dataMap;
  
  @Getter
  @Setter
  public List<String> nodeName;

  private JcrUtilityService jcrutil = new JcrUtilityService();
  
  @PostConstruct
  public void init() {

    try {
      Node node = resource.adaptTo(Node.class);
      log.debug("Multifield Model has been initiated : {}", node.getPath());

      String src = jcrutil.getProperty(node, "optionsSource");
      String isGroupingEnabled = jcrutil.getProperty(node, "grouping");

      if (src.equalsIgnoreCase("static")) {

        log.debug("Data fetching from static list");


        if (checkGroupingEnabled(isGroupingEnabled) && node.hasNode(GROUPEDOPTIONS)) {

          Node optionNode = node.getNode(GROUPEDOPTIONS) ;
          NodeIterator nodeItr = optionNode.getNodes();
          groupDataMap = new LinkedHashMap<>();
          populateGroupedHashMap(nodeItr);

          log.debug(LOGGER_CONSTANT, groupDataMap);
        } else if(node.hasNode(OPTIONS)){

          Node optionNode = node.getNode(OPTIONS);
          NodeIterator nodeItr = optionNode.getNodes();
          dataMap = new LinkedHashMap<>();
          nodeName = new ArrayList<>();
          populateHashMap(nodeItr);

          log.debug(LOGGER_CONSTANT, dataMap);
        }

      }

      else if (src.equalsIgnoreCase("genericlist")) {
        String contentPath = jcrutil.getProperty(node, "contentPath");
        Resource contentRes = resourceResolver.getResource(contentPath + "/jcr:content/list");
        Node listNode = contentRes.adaptTo(Node.class);

        log.debug("Node name - {}", listNode.getName());

        NodeIterator nodeItr = listNode.getNodes();

        if (checkGroupingEnabled(isGroupingEnabled)) {

          groupDataMap = new LinkedHashMap<>();
          log.debug("Group generic list");
          populateGroupedData(nodeItr);
          log.debug(LOGGER_CONSTANT, groupDataMap);
          
        } else {
          
          dataMap = new LinkedHashMap<>();
          log.debug("Non grouped generic list");
          populateHashMap(nodeItr);
          log.debug(LOGGER_CONSTANT, dataMap);
          
        }

      }

    } catch (Exception e) {
      log.error("Exception Caught :{}", e);
    }
  }

  private void populateGroupedData(NodeIterator nodeItr) throws RepositoryException {

    while (nodeItr.hasNext()) {

      Node n = nodeItr.nextNode();
      LinkedHashMap<String, String> listItems = new LinkedHashMap<>();

      String title = jcrutil.getProperty(n, TITLE);
      String value = jcrutil.getProperty(n, VALUE);
      listItems.put(title, value);

      addMapValue(jcrutil.getProperty(n, GROUP_NAME), listItems);

    }

  }

  private void populateHashMap(NodeIterator nodeItr) throws RepositoryException {

    while (nodeItr.hasNext()) {

      Node n = nodeItr.nextNode();

      String optionText = jcrutil.getProperty(n, TITLE);
      String optionValue = jcrutil.getProperty(n, VALUE);

      dataMap.put(optionText, optionValue);
      if(!StringUtils.isEmpty(jcrutil.getProperty(n, "intfieldId")))
        nodeName.add(jcrutil.getProperty(n, "intfieldId"));
    }

  }

  private void populateGroupedHashMap(NodeIterator nodeItr) throws RepositoryException {

    LinkedHashMap<String, String> listItems;

    while (nodeItr.hasNext()) {

      Node temp = nodeItr.nextNode();
      log.debug("Node name - {}, {}", temp.getName(), temp.getPath());
      NodeIterator nItr = temp.getNode("optionChild").getNodes();

      listItems = new LinkedHashMap<>();

      while (nItr.hasNext()) {

        Node n = nItr.nextNode();
        String optionText = jcrutil.getProperty(n, TITLE);
        String optionValue = jcrutil.getProperty(n, VALUE);
        listItems.put(optionText, optionValue);

      }

      String groupName = jcrutil.getProperty(temp, GROUP_NAME);
      groupDataMap.put(groupName, listItems);
    }
  }

  private void addMapValue(String key, LinkedHashMap<String, String> innerMap) {

    if (groupDataMap.isEmpty() || !groupDataMap.containsKey(key)) {

      groupDataMap.put(key, innerMap);
    } else {

      if (groupDataMap.containsKey(key)) {

        LinkedHashMap<String, String> inner = groupDataMap.get(key);

        for (Map.Entry<String, String> m : inner.entrySet()) {

          innerMap.put(m.getKey(), m.getValue());
        }
        groupDataMap.put(key, innerMap);
      }
    }
  }
  
  private boolean checkGroupingEnabled(String groupingFlag) {
    
    boolean flag = false;
    
    if(!StringUtils.isEmpty(groupingFlag) && groupingFlag.equalsIgnoreCase("true"))
      flag = true;
    else
      flag=false;
      
    return flag;    
  }
}


