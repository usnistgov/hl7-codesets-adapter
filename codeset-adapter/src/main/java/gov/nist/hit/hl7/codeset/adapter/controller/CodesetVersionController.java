package gov.nist.hit.hl7.codeset.adapter.controller;

import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetVersionRequest;
import gov.nist.hit.hl7.codeset.adapter.model.Codeset;
import gov.nist.hit.hl7.codeset.adapter.model.CodesetVersion;
import gov.nist.hit.hl7.codeset.adapter.serviceImpl.CodesetVersionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/codesets")
public class CodesetVersionController {

    private final CodesetVersionServiceImpl codesetVersionService;

    public CodesetVersionController(CodesetVersionServiceImpl codesetVersionService) {

        this.codesetVersionService = codesetVersionService;
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<CodesetVersion> addVersionToCodeset(@PathVariable String id,  @RequestBody CodesetVersionRequest request) throws IOException {
        CodesetVersion newCodesetVersion = this.codesetVersionService.addVersionToCodeset(id, request);
        return new ResponseEntity<>(newCodesetVersion, HttpStatus.CREATED);
    }

}
