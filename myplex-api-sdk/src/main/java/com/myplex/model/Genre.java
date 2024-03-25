
package com.myplex.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "type",
    "key",
    "humanReadable",
    "missing",
    "total",
    "other",
    "terms"
})

public class Genre {

    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private String type;
    @JsonProperty("key")
    private String key;
    @JsonProperty("humanReadable")
    private String humanReadable;
    @JsonProperty("missing")
    private Integer missing;
    @JsonProperty("total")
    private Integer total;
    @JsonProperty("other")
    private Integer other;
    @JsonProperty("terms")
    private List<Term> terms = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("humanReadable")
    public String getHumanReadable() {
        return humanReadable;
    }

    @JsonProperty("humanReadable")
    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    @JsonProperty("missing")
    public Integer getMissing() {
        return missing;
    }

    @JsonProperty("missing")
    public void setMissing(Integer missing) {
        this.missing = missing;
    }

    @JsonProperty("total")
    public Integer getTotal() {
        return total;
    }

    @JsonProperty("total")
    public void setTotal(Integer total) {
        this.total = total;
    }

    @JsonProperty("other")
    public Integer getOther() {
        return other;
    }

    @JsonProperty("other")
    public void setOther(Integer other) {
        this.other = other;
    }

    @JsonProperty("terms")
    public List<Term> getTerms() {
        return terms;
    }

    @JsonProperty("terms")
    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
