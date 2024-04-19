package gov.nist.hit.hl7.codeset.adapter.model.response;

import gov.nist.hit.hl7.codeset.adapter.model.VersionMetadata;

import java.util.List;

public class CodesetResponse {
    private String identifier;
    private String name;
    private VersionMetadata latestStableVersion;
    private List<VersionMetadata> versions;
    private List<CodeResponse> codes;
    private String codeMatchValue;

    public CodesetResponse() {
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

    public VersionMetadata getLatestStableVersion() {
        return latestStableVersion;
    }

    public void setLatestStableVersion(VersionMetadata latestStableVersion) {
        this.latestStableVersion = latestStableVersion;
    }

    public List<VersionMetadata> getVersions() {
        return versions;
    }

    public void setVersions(List<VersionMetadata> versions) {
        this.versions = versions;
    }

    public List<CodeResponse> getCodes() {
        return codes;
    }

    public void setCodes(List<CodeResponse> codes) {
        this.codes = codes;
    }

    public String getCodeMatchValue() {
        return codeMatchValue;
    }

    public void setCodeMatchValue(String codeMatchValue) {
        this.codeMatchValue = codeMatchValue;
    }
}
