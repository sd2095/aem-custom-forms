package com.forms.core.core.models;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.adobe.xfa.ut.StringUtils;
import com.forms.core.core.utils.JcrUtilityService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = {SlingHttpServletRequest.class},
defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Slf4j
public class FormAnalyticsModel {

  private static final String FIELD_ID = "fieldId";

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;
  
  @ValueMapValue
  @Getter
  @Setter
  private String formAnalyticsScript = "";
  
  private JcrUtilityService jcrutil = new JcrUtilityService();
  
  @PostConstruct
  public void init() {

    try {
      log.debug("Starting init method");
      Node node = resource.adaptTo(Node.class);
      String formAnalyticsStatus = jcrutil.getProperty(node, "formAnalytics");
      log.debug("Current Path of main form: {}, form analytics status: {}", node.getPath(),
          formAnalyticsStatus);
      if (!StringUtils.isEmpty(formAnalyticsStatus) && formAnalyticsStatus.equalsIgnoreCase("true")) {
        
        StringBuilder res = new StringBuilder();
        String formId = jcrutil.getProperty(node, FIELD_ID);          
        
        res.append("var ").append(formId+"product=\"not-available\";").append("\n");
        res.append("var form = {};").append("\n");        
        res.append("var ").append(formId+"Errors=[];").append("\n");
        res.append("var ").append("is"+formId+"Submitted=false;").append("\n");
        res.append("var ").append(formId+"Name=\""+jcrutil.getProperty(node, FIELD_ID)+"\";").append("\n");
        res.append("var ").append(formId+"Reason=\""+jcrutil.getProperty(node, "formReason")+"\";").append("\n");
        res.append("var ").append(formId+"SuccessType=\""+jcrutil.getProperty(node, "formType")+"\";").append("\n \n");
        
        if(node.hasNode("formAnalyticsTrigger")) {
          
          NodeIterator nodeItr = node.getNode("formAnalyticsTrigger").getNodes();
          while(nodeItr.hasNext()) {
            
            Node n = nodeItr.nextNode();
            String cssClass = jcrutil.getProperty(n, "componentCss");
            
            res.append("$('#"+formId+" .").append(cssClass).append("').on('change', function(event){\n");            
            res.append("\t\t logAnalyticsFormStart("+formId+"Name, "+formId+"product, "+formId+"Reason, \"\", \"\");\n");
            res.append("$('#"+formId+" ."+cssClass+"').unbind( 'change');\n});\n\n");
                        
          }
        }
        
        res.append("$('#").append(formId+"-submitBtn").append("').on('click', function(){\n");
        res.append("\t").append("updateFormAttributes(").append(formId+"Name, "+formId+"product, "+formId+"Reason, \"\", \"\");\n");
        res.append("\t").append("if ((!is"+formId+"Submitted) && PubSub) {\n");
        res.append("\t\t").append("is"+formId+"Submitted = true;\n");
        res.append("\t\t").append("PubSub.publish('analytics.formSubmit', digitalData.page.form);\n\t}\n");
        res.append("});\n");
                               
        
        formAnalyticsScript = res.toString();
        log.debug("Constraint : {}", formAnalyticsScript);
      } else {
        log.debug("Form analytics not enabled in the form - {}", node.getProperty(FIELD_ID).getString());
      }
    } catch (Exception ex) {
      log.error("Exception Caught : {}", ex);
    }
  }
}
