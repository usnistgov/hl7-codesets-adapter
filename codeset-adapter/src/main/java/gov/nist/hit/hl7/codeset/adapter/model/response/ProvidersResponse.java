package gov.nist.hit.hl7.codeset.adapter.model.response;

import gov.nist.hit.hl7.codeset.adapter.model.VersionMetadata;

import java.util.List;

public class ProvidersResponse {
    private String id;
    private String label;


    public ProvidersResponse() {
    }

    public ProvidersResponse(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
