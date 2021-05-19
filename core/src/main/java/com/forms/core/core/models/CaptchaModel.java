package com.forms.core.core.models;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Model(adaptables = SlingHttpServletRequest.class)
@Slf4j
public class CaptchaModel {
  
  @Self
  SlingHttpServletRequest request;
  
  @Getter
  @Setter
  private boolean showGoogleCaptcha = true;
  
  @PostConstruct
  public void init() {

    log.debug("Starting resolver utiltiy .. ");
          
    List<String> countryCode = new ArrayList<>();
    countryCode.add("ZH");
    countryCode.add("JA");
    countryCode.add("JP");
    countryCode.add("NA");
    
    String requestCountryCode = "";
    
    Cookie[] cookie = request.getCookies();
      
    for(Cookie c : cookie) {
      
      if(c.getName().equalsIgnoreCase("countryCode")) {
        requestCountryCode = c.getValue();
      }
      log.debug("Cookie name- {}, cookie value - {}",c.getName(), c.getValue());
    }
    
    for(String str : countryCode) {
      if(str.equalsIgnoreCase(requestCountryCode)) {
        showGoogleCaptcha = false;
      }
    }
    
    log.debug("Google captcha enabled - {}",showGoogleCaptcha);
  }

}
