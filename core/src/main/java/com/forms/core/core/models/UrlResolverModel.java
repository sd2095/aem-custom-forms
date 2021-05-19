package com.forms.core.core.models;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = SlingHttpServletRequest.class)
@Slf4j
public class UrlResolverModel {
  
  @Inject
  private String[] selectors;

  @Inject
  private String url;
  
  @Self
  private SlingHttpServletRequest request;

  @Getter
  @Setter
  public String redirecturl;

  @PostConstruct
  public void init() {

    log.debug("Starting resolver utiltiy ..");
    String uri = request.getRequestURI();
    
    try {      

      log.debug("Parameters - selectors -{}, configured url - {}", selectors, url);
      if(!StringUtils.isEmpty(url)) {
        
        StringBuilder sb = new StringBuilder(url);
        
        if(selectors.length > 0) {
          log.debug("Selectors coming from sightly");
          for(String str : selectors) {
            sb.append(".").append(str);
          }
        }
        else {
          if(uri.indexOf('.') > -1) {
            log.debug("Sightly selector is empty , reading url for any selectors");
            List<String> arr = new ArrayList<>();
            for(String str : uri.split("\\.")) {
              log.debug(str);
              if(!(str.contains("content") || str.contains("html"))) {                
                arr.add(str);
              }
            }
            
            for(String str : arr) {
              sb.append(".").append(str);
            }
          }
        }
        sb.append(".html");
        redirecturl = sb.toString();
        
        log.debug("Final url - {}",redirecturl);
      }
      else {
        log.debug("Url passed from sightly code is empty string.");
      }
      
    } catch (Exception e) {
      log.error("Exception caught - {}", e);
    }
  }
}
