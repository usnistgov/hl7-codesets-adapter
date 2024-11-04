package gov.nist.hit.hl7.codeset.adapter.serviceImpl;

import com.caucho.hessian.client.HessianProxyFactory;
import gov.cdc.vocab.service.VocabService;
import gov.cdc.vocab.service.bean.CodeSystem;
import gov.cdc.vocab.service.bean.ValueSet;
import gov.cdc.vocab.service.bean.ValueSetConcept;
import gov.cdc.vocab.service.bean.ValueSetVersion;
import gov.cdc.vocab.service.dto.input.CodeSystemSearchCriteriaDto;
import gov.cdc.vocab.service.dto.output.ValueSetResultDto;
import gov.cdc.vocab.service.dto.output.ValueSetVersionResultDto;
import gov.nist.hit.hl7.codeset.adapter.model.Code;
import gov.nist.hit.hl7.codeset.adapter.model.Codeset;
import gov.nist.hit.hl7.codeset.adapter.model.CodesetVersion;
import gov.nist.hit.hl7.codeset.adapter.model.VersionMetadata;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetMetadataResponse;
import gov.nist.hit.hl7.codeset.adapter.utils.NewPhinvadsScheduler;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service

public class PhinvadsWebServiceClient {

    private VocabService service;
    private static final Logger log = LoggerFactory.getLogger(PhinvadsWebServiceClient.class);
    @Autowired
    MongoOperations mongoOps;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


    public PhinvadsWebServiceClient() throws NoSuchAlgorithmException, KeyManagementException {
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

    // @PostConstruct
    public void initPhinvads() throws IOException {
        System.out.println("************ INIT PHINVADS VALUESET METADATA");
        updateCodesets();

    }

    // Every 1:00 AM Saturday
//    @Scheduled(cron = "0 0 1 * * SAT")
    public void updateCodesets() {
        log.info("Getting codesets from Phinvads Web service {}" + dateFormat.format(new Date()));
        getAllCodesetsWithVersions();

    }

    public List<CodesetMetadataResponse> getAllCodesetsWithVersions() {
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
            Codeset codeset = mongoOps.findOne(Query.query(Criteria.where("phinvadsOid").is(oid)),
                    Codeset.class);
            if (codeset != null) {
                log.info("Codeset Already exists: " + oid);
            } else {
                log.info("New Codeset found: " + oid);
                codeset = new Codeset();
                codeset.setVersions(new ArrayList<VersionMetadata>());
                codeset.setName(valueSet.getCode());
                codeset.setDescription(valueSet.getName());
                codeset.setPhinvadsOid(valueSet.getOid());
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
                    codesetVersion = new CodesetVersion();
                    codesetVersion.setVersion(String.valueOf(valueSetVersion.getVersionNumber()));
                    codesetVersion.setDateUpdated(valueSetVersion.getStatusDate());
                    codesetVersion.setCodesetId(codeset.getId());

                    CodesetVersion savedCodesetVersion = mongoOps.save(codesetVersion);

                    // Retrieve concept for the version
                    List<ValueSetConcept> valueSetConcepts = this.service
                            .getValueSetConceptsByValueSetVersionId(valueSetVersion.getId(), 1, Integer.MAX_VALUE)
                            .getValueSetConcepts();


                    VersionMetadata versionMetadata = new VersionMetadata(versionNumber, valueSetVersion.getStatusDate(), valueSetConcepts.size());
                    codeset.getVersions().add(versionMetadata);
                    if(codeset.getLatestVersion() == null){
                        codeset.setLatestVersion(versionMetadata);
                    } else {
                        if(Integer.parseInt(versionNumber) > Integer.parseInt(codeset.getLatestVersion().getVersion())){
                            codeset.setLatestVersion(versionMetadata);
                        }
                    }

                    // Get code systems and save all codes
                    Set<String> codeSystemOids = new HashSet<>();
                    Map<String, CodeSystem> uniqueIdCodeSystemMap = new HashMap<>();
                    List<Code> codes = new ArrayList<Code>();
                    for (ValueSetConcept pcode : valueSetConcepts) {
                        if (uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()) == null) {
                            CodeSystemSearchCriteriaDto csSearchCritDto = new CodeSystemSearchCriteriaDto();
                            csSearchCritDto.setCodeSearch(false);
                            csSearchCritDto.setNameSearch(false);
                            csSearchCritDto.setOidSearch(true);
                            csSearchCritDto.setDefinitionSearch(false);
                            csSearchCritDto.setAssigningAuthoritySearch(false);
                            csSearchCritDto.setTable396Search(false);
                            csSearchCritDto.setSearchType(1);
                            csSearchCritDto.setSearchText(pcode.getCodeSystemOid());
                            CodeSystem cs = this.service.findCodeSystems(csSearchCritDto, 1, 5).getCodeSystems().get(0);
                            uniqueIdCodeSystemMap.put(pcode.getCodeSystemOid(), cs);
                        }
                        Code code = new Code();
                        code.setValue(pcode.getConceptCode());
                        code.setDescription(pcode.getCodeSystemConceptName());
                        code.setComments(pcode.getDefinitionText());
//					code.setUsage("P");
                        code.setCodeSystem(uniqueIdCodeSystemMap.get(pcode.getCodeSystemOid()).getHl70396Identifier());
                        code.setCodesetversionId(savedCodesetVersion.getId());
                        codes.add(code);
                    }
                    mongoOps.insertAll(codes);
                    codeset.getCodeSetVersions().add(savedCodesetVersion);
                    log.info("Codeset: " + oid + " version: " + versionNumber + " has been added");
                    mongoOps.save(codeset);

                }
            }

            mongoOps.save(codeset);
        }

        return valueSetsWithVersions;
    }

}
