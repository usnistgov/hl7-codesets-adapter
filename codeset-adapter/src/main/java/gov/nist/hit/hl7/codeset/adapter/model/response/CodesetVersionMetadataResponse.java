package gov.nist.hit.hl7.codeset.adapter.model.response;

import gov.nist.hit.hl7.codeset.adapter.model.VersionMetadata;

import java.util.Date;
import java.util.List;

public class CodesetVersionMetadataResponse {
    private String identifier;
    private String name;
    private Date date;
    private int numberOfCodes;
    private String version;


    public CodesetVersionMetadataResponse() {
    }

    public String getId() {
        return identifier;
    }

    public void setId(String id) {
        this.identifier = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNumberOfCodes() {
        return numberOfCodes;
    }

    public void setNumberOfCodes(int numberOfCodes) {
        this.numberOfCodes = numberOfCodes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
