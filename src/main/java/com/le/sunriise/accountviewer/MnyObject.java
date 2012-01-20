package com.le.sunriise.accountviewer;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class MnyObject {
    @JsonIgnore
    protected Map<String, Object> columnValues = new HashMap<String, Object>();
}
