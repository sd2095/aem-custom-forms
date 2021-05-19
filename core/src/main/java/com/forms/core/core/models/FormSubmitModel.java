package com.forms.core.core.models;

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
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.forms.core.core.utils.JcrUtilityService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = {SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Slf4j
public class FormSubmitModel {

  private static final String JQCST = "\t$('#";

  @SlingObject
  private ResourceResolver resourceResolver;

  @SlingObject
  private Resource resource;

  @ValueMapValue
  @Getter
  @Setter
  private String formAction = "";

  private StringBuilder res = new StringBuilder();
  private boolean checkAnalytics = false;

  private JcrUtilityService jcrutil = new JcrUtilityService();
  
  @PostConstruct
  public void init() {


    try {

      log.debug("Starting init method");
      Node node = resource.adaptTo(Node.class);
      String formActionType = jcrutil.getProperty(node, "formActionType");
      log.debug("Form Action Type - {}", formActionType);

      if (!StringUtils.isEmpty(formActionType) && !formActionType.equalsIgnoreCase("jscript")) {

        createScript(node, formActionType);
        formAction = res.toString();

      }
    }

    catch (Exception e) {

      log.error("Exception Caught {}", e);
    }
  }

  private void createScript(Node root, String actionType) throws RepositoryException {

    log.debug("Started creating the script {}", actionType);
    String fieldId = jcrutil.getProperty(root, "fieldId");
    String servleturl = jcrutil.getProperty(root, "servletUrl");
    String formPrePopulate = jcrutil.getProperty(root, "prePopulate");
    checkAnalytics = jcrutil.getProperty(root, "formAnalytics").equalsIgnoreCase("true");
    generateAutoFillScript(root, formPrePopulate);
    ajaxStart(fieldId, servleturl);
    if (actionType.equalsIgnoreCase("redit")) {

      String customScript = jcrutil.getProperty(root, "script");
      res.append(customScript);

    }
    else {
      if(root.hasNode("success_modal_config"))
        generateModalConditions(root.getNode("success_modal_config"));
    }
    if (checkAnalytics) {
      res.append("\t\t\t\tlogAnalyticsFormSuccess(").append(fieldId + "Name,")
          .append(fieldId + "product,").append(fieldId + "Reason,").append("msg").append(",\"\");");
    }
    res.append("\t\t\t\t},\n\t\t\t");
    ajaxError(root, fieldId);
    res.append("\t\t\t\tcomplete: function() { \n");
    res.append("\t\t\t\t\t$('.fts-loader').hide();\n}\n");
    res.append("\t\t\t});\n\t\t}\n\t});\n});");

  }

  private void generateModalConditions(Node node) throws RepositoryException {

    NodeIterator nodeItr = node.getNodes();
    while(nodeItr.hasNext()) {
      Node n = nodeItr.nextNode();
      String modalMsg = jcrutil.getProperty(n, "modal_msg");
      String modalFieldId = jcrutil.getProperty(n, "modal_fieldId");
      String modalId = "modal-"+modalFieldId;
      res.append("\n if(").append(modalMsg).append(") {\n");
      res.append("\t\t\t $('#").append(modalId).append("').show();\n");
      res.append("}\n");
    }
    
  }

  private void ajaxStart(String fieldId, String servleturl) {

    res.append("$('#" + fieldId + "').each(function () { \n");
    res.append("\t$(this).validate({\n");
    res.append("\t\tignore: \":not(:visible)\",\n");
    res.append("\t\tonkeyup: function (element) {\n");
    res.append("\t\t\t$(element).valid()\n\t\t},\n");
    res.append("\t\tonclick: function (element) {\n");
    res.append("\t\t\t$(element).valid()\n\t\t},\n");
    res.append("\t\trules: constraintsJSON.rules,").append("\n\t\t")
        .append("messages: constraintsJSON.messages,\n");
    res.append("\t\tsubmitHandler: function (form) {\n");
    res.append("\t\t\t$.ajax({\n");
    res.append("\t\t\t\turl: \"" + servleturl + "\",\n");
    res.append("\t\t\t\ttype: \"POST\",\n");
    res.append("\t\t\t\tdataType: \"json\",\n");
    res.append("\t\t\t\tdata: $('#" + fieldId + "').serialize(),\n");
    res.append("\t\t\t\tbeforeSend: function() {\n");
    res.append("\t\t\t\t $('#"+fieldId+"-submitBtn').attr('disabled',true);\n");
    res.append("\t\t\t\t\t$('.fts-loader').show();\n\t\t\t},\n");
    res.append("\t\t\t\tsuccess: function (msg) {\n");    

  }

  private void ajaxError(Node root, String fieldId) throws RepositoryException {

    res.append("\terror: function (jqXHR, textStatus, errorThrown) {\n");
    if(root.hasNode("error_modal_config"))
      generateModalConditions(root.getNode("error_modal_config"));
    if (checkAnalytics) {
      res.append("\t\t\t\t\tlogAnalyticsFormError(").append(fieldId + "Name,")
          .append(fieldId + "product,").append(fieldId + "Reason,").append("\"\",")
          .append("\"server-error\");").append("\n");
    }
    res.append("\t\t\t\t},\n");

  }

  private void generateAutoFillScript(Node node, String formPrePopulate)
      throws RepositoryException {

    if (formPrePopulate.equalsIgnoreCase("true")) {

      log.debug("Auto Form filling enabled.. Generating script");


      if (node.hasNode("param_map")) {

        NodeIterator nodeItr = node.getNode("param_map").getNodes();

        while (nodeItr.hasNext()) {

          Node n = nodeItr.nextNode();
          String paramKey = jcrutil.getProperty(n, "param_key");
          String elementMap = jcrutil.getProperty(n, "element_map");
          String prePopulateSource = jcrutil.getProperty(n, "formsrc");

          if (prePopulateSource.equalsIgnoreCase("contexthub")) {

            log.debug("Auto filling data from context hub data");

            res.append("getContextHubData();\n");
            res.append("if(userDataCntxtHub && validateParam(").append(paramKey).append(")){\n");
            res.append(JQCST).append(elementMap).append("').val(" + paramKey + ");\n");
            res.append(JQCST).append(elementMap)
                .append("').parents('.fts-form__group').addClass('focused');");
            res.append("\n}\n");
          } else {

            log.debug("Auto filling data from url parameter");

            res.append("if(validateParam($.urlParam('" + paramKey + "'))){\n");
            res.append(JQCST).append(elementMap)
                .append("').val(validateParam($.urlParam('" + paramKey + "')));\n");
            res.append(JQCST).append(elementMap)
                .append("').parents('.fts-form__group').addClass('focused');");
            res.append("\n}\n");
          }
        }
      }
    }
  }

}
