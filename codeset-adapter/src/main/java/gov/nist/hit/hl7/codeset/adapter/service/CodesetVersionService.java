package gov.nist.hit.hl7.codeset.adapter.service;


import gov.nist.hit.hl7.codeset.adapter.model.CodesetVersion;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetVersionRequest;

public interface CodesetVersionService {
    public CodesetVersion addVersionToCodeset(String codesetId, CodesetVersionRequest codesetVersionRequest);
    public CodesetVersion getVersionDetails(String codesetId, String version);

}
