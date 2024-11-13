package gov.nist.hit.hl7.codeset.adapter.controller;

import gov.nist.hit.hl7.codeset.adapter.model.Codeset;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetRequest;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetSearchCriteria;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetMetadataResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetVersionMetadataResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.ProvidersResponse;
import gov.nist.hit.hl7.codeset.adapter.serviceImpl.CodesetServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CodesetController {
    private final CodesetServiceImpl codesetService;

    public CodesetController(CodesetServiceImpl codesetService) {
        this.codesetService = codesetService;
    }
    @GetMapping("/providers")
    public ResponseEntity<List<ProvidersResponse>> getProviders() throws IOException {
        List<ProvidersResponse> providers = codesetService.getProviders();
        return new ResponseEntity<>(providers, HttpStatus.OK);
    }

    @GetMapping("/{provider}/codesets")
    public ResponseEntity<List<CodesetMetadataResponse>> getCodesets(@PathVariable String provider, @ModelAttribute CodesetSearchCriteria criteria) throws IOException {
        List<CodesetMetadataResponse> codesets = codesetService.getCodesets(provider, criteria);
        return new ResponseEntity<>(codesets, HttpStatus.OK);
    }

    @GetMapping("/{provider}/codesets/{id}/metadata")
    public ResponseEntity<CodesetMetadataResponse> getCodesetMetadata(@PathVariable String provider,@PathVariable String id) throws IOException {
        CodesetMetadataResponse codeset = codesetService.getCodesetMetadata(provider, id);
        return new ResponseEntity<>(codeset, HttpStatus.OK);
    }
    @GetMapping("/{provider}/codesets/{id}/versions/{version}/metadata")
    public ResponseEntity<CodesetVersionMetadataResponse> getCodesetMetadata(@PathVariable String provider,@PathVariable String id,@PathVariable String version) throws IOException {
        CodesetVersionMetadataResponse codeset = codesetService.getCodesetVersionMetadata(provider, id, version);
        return new ResponseEntity<>(codeset, HttpStatus.OK);
    }

    @GetMapping("/{provider}/codesets/{id}")
    public ResponseEntity<CodesetResponse> getCodeset(@PathVariable String provider,@PathVariable String id, @ModelAttribute CodesetSearchCriteria criteria) throws IOException {
        CodesetResponse codeset = codesetService.getCodeset(provider, id, criteria);
        return new ResponseEntity<>(codeset, HttpStatus.OK);
    }


}
