package com.forms.core.core.servlets;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import lombok.extern.slf4j.Slf4j;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/forms/captchaverification")
@Designate(ocd = RecaptchaVerificationServlet.Config.class)
@Slf4j
public class RecaptchaVerificationServlet extends SlingAllMethodsServlet {

  private static final long serialVersionUID = 2449004195470167982L;

  @ObjectClassDefinition(name = "ReCaptcha Verification Config",
      description = "Configuration for ReCaptcha Private Key")
  public @interface Config {
    @AttributeDefinition(name = "ReCaptcha Private Key", description = "ReCaptcha Private Key")
    String captchaPrivateKey()

    default "6Ld5tL0UAAAAAPT53pPJ23c5LQxzWvyftfTYL0RN";

    @AttributeDefinition(name = "Google Captcha Verification URL")
    String googleVerificationUrl() default "https://www.google.com/recaptcha/api/siteverify";
  }

  private String privateKey = "";
  private String serverValidationUrl = "";

  @Activate
  protected void activate(Config config) {
    this.privateKey = config.captchaPrivateKey();
    this.serverValidationUrl = config.googleVerificationUrl();
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    CloseableHttpClient httpClient = HttpClients.createDefault();

    try {

      String captchaResponse = request.getParameter("captchaResponse");

      String clientUrl =
          serverValidationUrl + "?secret=" + privateKey + "&response=" + captchaResponse;
      log.debug("Url in get method : {}", clientUrl);

      HttpGet httpGet = new HttpGet(clientUrl);
      httpGet.addHeader("Accept", "application/json");

      CloseableHttpResponse clientResponse = httpClient.execute(httpGet);
      int statusCode = clientResponse.getStatusLine().getStatusCode();
      String result = "";

      if (statusCode == HttpStatus.SC_OK) {

        HttpEntity entity = clientResponse.getEntity();
        result = EntityUtils.toString(entity);
        log.debug("Response found : {}", result);
        dispatchResponse(response, result);
      } else {
        result = "Invalid Response";
        log.debug("Invalid Response {}", clientResponse.getStatusLine().getStatusCode());
        dispatchResponse(response, result);
      }

    } catch (Exception e) {
      log.error("Exception Caught in doGet method {}", e);
    } finally {
      httpClient.close();
    }

  }

  private void dispatchResponse(SlingHttpServletResponse response, String result)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(result);
    response.getWriter().flush();
  }
}
