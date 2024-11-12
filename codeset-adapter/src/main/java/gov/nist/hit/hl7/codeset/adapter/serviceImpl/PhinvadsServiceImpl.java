package gov.nist.hit.hl7.codeset.adapter.serviceImpl;

import com.caucho.hessian.client.HessianProxyFactory;
import gov.cdc.vocab.service.VocabService;
import gov.cdc.vocab.service.bean.CodeSystem;
import gov.cdc.vocab.service.bean.ValueSet;
import gov.cdc.vocab.service.bean.ValueSetConcept;
import gov.cdc.vocab.service.bean.ValueSetVersion;
import gov.cdc.vocab.service.dto.input.CodeSystemSearchCriteriaDto;
import gov.cdc.vocab.service.dto.input.ValueSetConceptSearchCriteriaDto;
import gov.cdc.vocab.service.dto.input.ValueSetSearchCriteriaDto;
import gov.cdc.vocab.service.dto.input.ValueSetVersionSearchCriteriaDto;
import gov.cdc.vocab.service.dto.output.ValueSetConceptResultDto;
import gov.cdc.vocab.service.dto.output.ValueSetResultDto;
import gov.cdc.vocab.service.dto.output.ValueSetVersionResultDto;
import gov.nist.hit.hl7.codeset.adapter.model.*;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetSearchCriteria;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetMetadataResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetResponse;
import gov.nist.hit.hl7.codeset.adapter.repository.CodesetRepository;
import gov.nist.hit.hl7.codeset.adapter.repository.CodesetVersionRepository;
import gov.nist.hit.hl7.codeset.adapter.service.ProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

@Service

public class PhinvadsServiceImpl implements ProviderService {

    private VocabService service;
    private static final Logger log = LoggerFactory.getLogger(PhinvadsServiceImpl.class);
    @Autowired
    MongoOperations mongoOps;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final CodesetRepository codesetRepository;

    private final CodesetVersionRepository codesetVersionRepository;


    public PhinvadsServiceImpl(CodesetRepository codesetRepository, CodesetVersionRepository codesetVersionRepository) throws NoSuchAlgorithmException, KeyManagementException {
        this.codesetRepository = codesetRepository;
        this.codesetVersionRepository = codesetVersionRepository;
        String serviceUrl = "https://phinvads.cdc.gov/vocabService/v2";
        /* Start of Fix */
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

        }};


        HessianProxyFactory factory = new HessianProxyFactory();
        try {
            SSLContext sc = SSLContext.getInstance("SSL");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            /* End of the fix*/
            this.service = (VocabService) factory.create(VocabService.class, serviceUrl);
//			mongoOps = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), "vocabulary-service"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

//            @PostConstruct
    public void initPhinvads() throws IOException {
        System.out.println("************ INIT PHINVADS VALUESET METADATA");
        updateCodesets();
    }

    // Every 1:00 AM Saturday
//    @Scheduled(cron = "0 0 1 * * SAT")
    public void updateCodesets() throws IOException {
        log.info("Getting codesets from Phinvads Web service {}" + dateFormat.format(new Date()));
        // Step 1: Retrieve all ValueSets
        List<ValueSet> allValueSets = this.service.getAllValueSets().getValueSets();

        // Step 2: Retrieve all ValueSetVersions in one call
        List<ValueSetVersion> allValueSetVersions = this.service.getAllValueSetVersions().getValueSetVersions();

        // Step 3: Prepare response list
        List<CodesetMetadataResponse> valueSetsWithVersions = new ArrayList<>();

        // Step 4: Loop through each ValueSet and populate CodesetMetadataResponse with versions and concept counts
        for (ValueSet valueSet : allValueSets) {
            String oid = valueSet.getOid();
            String name = valueSet.getName();

            // Check DB if Codeset exist
            Codeset codeset = mongoOps.findOne(Query.query(Criteria.where("identifier").is(oid)),
                    Codeset.class);
            if (codeset != null) {
                log.info("Codeset Already exists: " + oid);
            } else {
                log.info("New Codeset found: " + oid);
                codeset = new Codeset();
                codeset.setVersions(new ArrayList<VersionMetadata>());
                codeset.setName(valueSet.getCode());
                codeset.setDescription(valueSet.getName());
                codeset.setIdentifier(valueSet.getOid());
                codeset.setProvider("phinvads");
                codeset.setDateUpdated(valueSet.getStatusDate());
                codeset.setCodeSetVersions(new HashSet<CodesetVersion>());
            }
            codeset = mongoOps.save(codeset);
            List<ValueSetVersion> currentValueSetVersions = allValueSetVersions.stream().filter(version -> oid.equals(version.getValueSetOid())).toList();
            for (ValueSetVersion valueSetVersion : currentValueSetVersions) {
                String versionNumber = String.valueOf(valueSetVersion.getVersionNumber());
                CodesetVersion codesetVersion = mongoOps.findOne(Query.query(Criteria.where("version").is(versionNumber).and("codesetId").is(codeset.getId())),
                        CodesetVersion.class);

                if (codesetVersion != null) {
                    log.info("Codeset version Already exists " + versionNumber);
                    log.info("No action needed");

                } else {
                    log.info("New Codeset version found " + String.valueOf(valueSetVersion.getVersionNumber()));
                    codesetVersion = createNewCodesetVersion(codeset, valueSetVersion, false);
                    mongoOps.save(codeset);

                }
            }

            mongoOps.save(codeset);
        }

    }

    public CodesetVersion createNewCodesetVersion(Codeset codeset, ValueSetVersion valueSetVersion, Boolean saveCodes) throws IOException {
        CodesetVersion codesetVersion = new CodesetVersion();
        codesetVersion.setVersion(String.valueOf(valueSetVersion.getVersionNumber()));
        codesetVersion.setDateUpdated(valueSetVersion.getStatusDate());
        codesetVersion.setCodesetId(codeset.getId());

        CodesetVersion savedCodesetVersion = mongoOps.save(codesetVersion);

        // Retrieve concept for the version
        List<ValueSetConcept> valueSetConcepts = this.service
                .getValueSetConceptsByValueSetVersionId(valueSetVersion.getId(), 1, Integer.MAX_VALUE)
                .getValueSetConcepts();


        VersionMetadata versionMetadata = new VersionMetadata(String.valueOf(valueSetVersion.getVersionNumber()), valueSetVersion.getStatusDate(), valueSetConcepts.size());
        codeset.getVersions().add(versionMetadata);
        if (codeset.getLatestVersion() == null) {
            codeset.setLatestVersion(versionMetadata);
        } else {
            if (valueSetVersion.getVersionNumber() > Integer.parseInt(codeset.getLatestVersion().getVersion())) {
                codeset.setLatestVersion(versionMetadata);
            }
        }
        if (valueSetConcepts.size() > 500) {
            savedCodesetVersion.setCodesStatus(CodesetVersion.CodesStatus.NOT_NEEDED);
        } else if (saveCodes) {
            // Get code systems and save all codes
            Set<String> codeSystemOids = new HashSet<>();
            Map<String, CodeSystem> uniqueIdCodeSystemMap = new HashMap<>();
            List<Code> codes = new ArrayList<Code>();
            for (ValueSetConcept pcode : valueSetConcepts) {
                if (uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()) == null) {
                    CodeSystem cs = getCodeSystem(pcode.getCodeSystemOid());
                    uniqueIdCodeSystemMap.put(pcode.getCodeSystemOid(), cs);
                }
                Code code = new Code();
                code.setValue(pcode.getConceptCode());
                code.setDescription(pcode.getCodeSystemConceptName());
                code.setComments(pcode.getDefinitionText());
                code.setUsage("R");
                code.setCodeSystem(uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()).getHl70396Identifier());
                code.setCodesetversionId(savedCodesetVersion.getId());
                codes.add(code);
                savedCodesetVersion.setCodesStatus(CodesetVersion.CodesStatus.SAVED);
            }
            mongoOps.insertAll(codes);
        } else {
            savedCodesetVersion.setCodesStatus(CodesetVersion.CodesStatus.PENDING);
        }


        codeset.getCodeSetVersions().add(savedCodesetVersion);
        log.info("Codeset: " + codeset.getIdentifier() + " version: " + String.valueOf(valueSetVersion.getVersionNumber()) + " has been added");
        mongoOps.save(savedCodesetVersion);
        return codesetVersion;
    }

    @Override
    public String getLatestVersion(String id) throws IOException {
        ValueSetVersionSearchCriteriaDto criteria = new ValueSetVersionSearchCriteriaDto();
        criteria.setOidSearch(true);
        criteria.setSearchText(id);
        criteria.setSearchType(1);
        criteria.setVersionOption(3);
        ValueSetVersionResultDto valueset = this.service.findValueSetVersions(criteria, 1, 1);

        return (valueset.getValueSetVersion() != null) ? String.valueOf(valueset.getValueSetVersion().getVersionNumber()) : null;
    }

    @Override
    public List<CodesetMetadataResponse> getCodesets(CodesetSearchCriteria codesetSearchCriteria) throws IOException {
        return null;
    }

    @Override
    public Provider getProvider() {
        return new Provider("phinvads", "Phinvads");
    }

    public ValueSet getValueset(String id) throws IOException {
        ValueSetSearchCriteriaDto criteria = new ValueSetSearchCriteriaDto();
        criteria.setOidSearch(true);
        criteria.setSearchText(id);
        criteria.setSearchType(1);
        ValueSetResultDto valuesetDto = this.service.findValueSets(criteria, 1, 1);
        return valuesetDto.getValueSet();
    }

    public ValueSetVersion getValuesetVersion(String id, String version) throws IOException {
        ValueSetVersionSearchCriteriaDto criteria = new ValueSetVersionSearchCriteriaDto();
        criteria.setOidSearch(true);
        criteria.setSearchText(id);
        criteria.setSearchType(1);
        criteria.setVersionOption(1);
        ValueSetVersionResultDto valuesetDto = this.service.findValueSetVersions(criteria, 1, 1000);
        List<ValueSetVersion> valuesets = valuesetDto.getValueSetVersions();
        ValueSetVersion valuesetVersion = valuesets.stream().filter(v -> String.valueOf(v.getVersionNumber()).equals(version)).findFirst().orElse(null);
        return valuesetVersion;
    }

    public CodeSystem getCodeSystem(String codeSystemOid) {
        CodeSystemSearchCriteriaDto csSearchCritDto = new CodeSystemSearchCriteriaDto();
        csSearchCritDto.setCodeSearch(false);
        csSearchCritDto.setNameSearch(false);
        csSearchCritDto.setOidSearch(true);
        csSearchCritDto.setDefinitionSearch(false);
        csSearchCritDto.setAssigningAuthoritySearch(false);
        csSearchCritDto.setTable396Search(false);
        csSearchCritDto.setSearchType(1);
        csSearchCritDto.setSearchText(codeSystemOid);
        CodeSystem cs = this.service.findCodeSystems(csSearchCritDto, 1, 5).getCodeSystems().get(0);
        return cs;
    }

    @Override
    public void getCodesetAndSave(String id, String version) throws IOException {
        ValueSet valueset = getValueset(id);
        if (valueset == null) {
            return;
        }

        ValueSetVersion valuesetVersion = getValuesetVersion(id, version);
        if (valuesetVersion == null) {
            return;
        }

        Codeset codeset = codesetRepository.findByIdentifier(id).orElse(null);
        if (codeset == null) {
            codeset = new Codeset();
            codeset.setIdentifier(id);
            codeset.setVersions(new ArrayList<VersionMetadata>());
            codeset.setName(valueset.getCode());
            codeset.setDescription(valueset.getName());
            codeset.setProvider("phinvads");
            codeset.setDateUpdated(valueset.getStatusDate());
            codeset.setCodeSetVersions(new HashSet<CodesetVersion>());
            codeset = mongoOps.save(codeset);
        }
        CodesetVersion codesetVersion = codesetVersionRepository.findByCodesetIdAndVersion(codeset.getId(), version).orElse(null);
        if (codesetVersion == null) {
            codesetVersion = createNewCodesetVersion(codeset, valuesetVersion, true);
            mongoOps.save(codeset);
        } else {
            if (codesetVersion.getCodesStatus().equals(CodesetVersion.CodesStatus.PENDING)) {
                List<ValueSetConcept> valueSetConcepts = this.service
                        .getValueSetConceptsByValueSetVersionId(valuesetVersion.getId(), 1, Integer.MAX_VALUE)
                        .getValueSetConcepts();
                // Get code systems and save all codes
                Set<String> codeSystemOids = new HashSet<>();
                Map<String, CodeSystem> uniqueIdCodeSystemMap = new HashMap<>();
                List<Code> codes = new ArrayList<Code>();
                for (ValueSetConcept pcode : valueSetConcepts) {
                    if (uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()) == null) {
                        CodeSystem cs = getCodeSystem(pcode.getCodeSystemOid());
                        uniqueIdCodeSystemMap.put(pcode.getCodeSystemOid(), cs);
                    }
                    Code code = new Code();
                    code.setValue(pcode.getConceptCode());
                    code.setDescription(pcode.getCodeSystemConceptName());
                    code.setComments(pcode.getDefinitionText());
                    code.setUsage("R");
                    code.setCodeSystem(uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()).getHl70396Identifier());
                    code.setCodesetversionId(codesetVersion.getId());
                    codes.add(code);
                    codesetVersion.setCodesStatus(CodesetVersion.CodesStatus.SAVED);
                }
                mongoOps.insertAll(codes);
                mongoOps.save(codesetVersion);
            }
        }

    }

    @Override
    public List<Code> getCodes(String id, String version, String match) throws IOException {
        ValueSetVersion valuesetVersion = getValuesetVersion(id, version);
        if (valuesetVersion == null) {
            return new ArrayList<>();
        }
//        List<ValueSetConcept> valueSetConcepts = this.service
//                .getValueSetConceptsByValueSetVersionId(valuesetVersion.getId(), 1, Integer.MAX_VALUE)
//                .getValueSetConcepts();
        List<ValueSetConcept> valueSetConcepts = new ArrayList<>();
        if (match != null) {
            ValueSetConceptSearchCriteriaDto valueSetConceptSearchCriteriaDto = new ValueSetConceptSearchCriteriaDto();
            valueSetConceptSearchCriteriaDto.setSearchText(match);
            valueSetConceptSearchCriteriaDto.setSearchType(1);
            valueSetConceptSearchCriteriaDto.setVersionOption(3);
            valueSetConceptSearchCriteriaDto.setFilterByValueSets(true);
            valueSetConceptSearchCriteriaDto.setValueSetOids(Arrays.asList(id));
            valueSetConceptSearchCriteriaDto.setConceptCodeSearch(true);
            ValueSetConceptResultDto valueSetConceptResultDto = this.service.findValueSetConcepts(valueSetConceptSearchCriteriaDto, 1, Integer.MAX_VALUE);
            valueSetConcepts = valueSetConceptResultDto.getValueSetConcepts();

        } else {
            valueSetConcepts = this.service
                    .getValueSetConceptsByValueSetVersionId(valuesetVersion.getId(), 1, Integer.MAX_VALUE)
                    .getValueSetConcepts();
        }

        // Get code systems and save all codes
        Set<String> codeSystemOids = new HashSet<>();
        Map<String, CodeSystem> uniqueIdCodeSystemMap = new HashMap<>();
        List<Code> codes = new ArrayList<Code>();
        for (ValueSetConcept pcode : valueSetConcepts) {
            if(pcode.getValueSetVersionId().equals(valuesetVersion.getId())){
                if (uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()) == null) {
                    CodeSystem cs = getCodeSystem(pcode.getCodeSystemOid());
                    uniqueIdCodeSystemMap.put(pcode.getCodeSystemOid(), cs);
                }
                Code code = new Code();
                code.setValue(pcode.getConceptCode());
                code.setDescription(pcode.getCodeSystemConceptName());
                code.setComments(pcode.getDefinitionText());
                code.setUsage("R");
                code.setCodeSystem(uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()).getHl70396Identifier());
                codes.add(code);
            }

        }
        return codes;
    }

}
