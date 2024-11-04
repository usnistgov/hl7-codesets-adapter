package gov.nist.hit.hl7.codeset.adapter.serviceImpl;

import gov.cdc.vocab.service.bean.ValueSet;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetSearchCriteria;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetMetadataResponse;
import gov.nist.hit.hl7.codeset.adapter.service.ProviderService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PhinvadsServiceImpl implements ProviderService {
    private final PhinvadsWebServiceClient phinvadsClient;

    public PhinvadsServiceImpl( PhinvadsWebServiceClient phinvadsClient) {
        this.phinvadsClient = phinvadsClient;
    }

    @Override
    public List<CodesetMetadataResponse> getCodesets(CodesetSearchCriteria codesetSearchCriteria) throws IOException {
        phinvadsClient.getAllCodesetsWithVersions();
        return null;
    }
}
