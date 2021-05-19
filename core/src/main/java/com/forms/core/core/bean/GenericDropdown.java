package com.forms.core.core.bean;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class GenericDropdown {

  public GenericDropdown(String id, String text) {
    setId(id);
    setText(text);
  }
  
  public GenericDropdown(String parent, String text, String id) {
    setId(id);
    setText(text);
    setParent(parent);
  }
  
  public GenericDropdown(String text, List<GenericDropdown> children) {
    
    setChildren(children);
    setText(text);
  }

  @Getter
  @Setter
  private String id;

  @Getter
  @Setter
  private String text;
  
  @Getter
  @Setter
  private String grpName;
  
  @Getter
  @Setter
  private String parent;
  
  @Getter
  @Setter
  private List<GenericDropdown> children;
  
  @Getter
  @Setter
  private String data;
  
  @Getter
  @Setter
  private GenericDropdown obj;
  
}
