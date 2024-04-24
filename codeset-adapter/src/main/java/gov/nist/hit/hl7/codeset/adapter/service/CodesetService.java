package gov.nist.hit.hl7.codeset.adapter.service;


import gov.nist.hit.hl7.codeset.adapter.model.Codeset;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetRequest;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetSearchCriteria;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.ProvidersResponse;

import java.io.IOException;
import java.util.List;

public interface CodesetService {
    public List<ProvidersResponse> getProviders() throws IOException;
    public List<CodesetResponse> getCodesets(String provider, CodesetSearchCriteria codesetSearchCriteria) throws IOException;
    public CodesetResponse getCodesetMetadata(String provider, String id) throws IOException;
    public CodesetResponse getCodeset(String provider, String id, CodesetSearchCriteria criteria) throws IOException;


}
