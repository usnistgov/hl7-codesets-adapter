package gov.nist.hit.hl7.codeset.adapter.service;

import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetSearchCriteria;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetMetadataResponse;

import java.io.IOException;
import java.util.List;

public interface ProviderService {
    public List<CodesetMetadataResponse> getCodesets(CodesetSearchCriteria codesetSearchCriteria) throws IOException;

}
