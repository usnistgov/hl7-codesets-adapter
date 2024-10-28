package gov.nist.hit.hl7.codeset.adapter.serviceImpl;

import gov.nist.hit.hl7.codeset.adapter.model.Code;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetRequest;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodeResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetMetadataResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.CodesetResponse;
import gov.nist.hit.hl7.codeset.adapter.model.response.ProvidersResponse;
import gov.nist.hit.hl7.codeset.adapter.repository.CodesetRepository;
import gov.nist.hit.hl7.codeset.adapter.repository.CodesetVersionRepository;
import gov.nist.hit.hl7.codeset.adapter.model.Codeset;
import gov.nist.hit.hl7.codeset.adapter.model.CodesetVersion;
import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetSearchCriteria;
import gov.nist.hit.hl7.codeset.adapter.service.CodesetService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CodesetServiceImpl implements CodesetService {
    private final CodesetRepository codesetRepository;
    private final CodesetVersionRepository codesetVersionRepository;
    private final MongoTemplate mongoTemplate;

    public CodesetServiceImpl(CodesetRepository codesetRepository, CodesetVersionRepository codesetVersionRepository, MongoTemplate mongoTemplate) {
        this.codesetRepository = codesetRepository;
        this.codesetVersionRepository = codesetVersionRepository;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public List<ProvidersResponse> getProviders() throws IOException {
        List<ProvidersResponse> providers = new ArrayList<ProvidersResponse>();
        providers.add(new ProvidersResponse("phinvads", "Phinvads"));
        return providers;
    }

    @Override
    public List<CodesetMetadataResponse> getCodesets(String provider, CodesetSearchCriteria criteria) throws IOException {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("provider").regex("^" + Pattern.quote(provider) + "$", "i")
        );

        ProjectionOperation projectionOperation = Aggregation.project()
                .and("name").as("name")
                .and("latestVersion").as("latestStableVersion");

        if (provider != null && provider.equalsIgnoreCase("phinvads")) {
            projectionOperation = projectionOperation.and("phinvadsOid").as("identifier");
        } else {

        }


        // Execute the aggregation
        Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectionOperation);
        AggregationResults<CodesetMetadataResponse> results = mongoTemplate.aggregate(aggregation, "codeset", CodesetMetadataResponse.class);
        return results.getMappedResults();
    }

    @Override
    public CodesetMetadataResponse getCodesetMetadata(String provider, String id) throws IOException {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("provider").regex("^" + Pattern.quote(provider) + "$", "i"), // Adjust "someField" to the actual field for provider
                Criteria.where("phinvadsOid").is(id)
        );
        // Define the aggregation with match and projection
        MatchOperation matchOperation = Aggregation.match(criteria);
        ProjectionOperation projectionOperation = Aggregation.project()
                .and("name").as("name")
                .and("latestVersion").as("latestStableVersion")
                .and("versions").as("versions");

        if (provider != null && provider.equalsIgnoreCase("phinvads")) {
            projectionOperation = projectionOperation.and("phinvadsOid").as("identifier");
        } else {

        }

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectionOperation
        );

        // Execute the aggregation and expect only one result
        AggregationResults<CodesetMetadataResponse> results = mongoTemplate.aggregate(aggregation, "codeset", CodesetMetadataResponse.class);
        List<CodesetMetadataResponse> resultList = results.getMappedResults();

        // Return the first result or null if no results
        return resultList.isEmpty() ? null : resultList.get(0);

    }
//    public List<Codeset> getCodesets(CodesetSearchCriteria criteria) throws IOException {
//        return codesetRepository.findAll((Specification<Codeset>) (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (criteria.getScope() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("scope"), criteria.getScope()));
//            }
//            if (criteria.getVersion() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("versionNumber"), criteria.getVersion()));
//            }
//            if (criteria.getName() != null) {
//                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        });
//    }


    public CodesetResponse getCodeset(String provider, String id, CodesetSearchCriteria searchCriteria) throws IOException {
        // Step 1: Initial match criteria for the Codeset based on provider and ID
        Criteria criteria = Criteria.where("provider").regex("^" + Pattern.quote(provider) + "$", "i")
                .and("phinvadsOid").is(id);

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));

        // Step 2: Lookup for CodesetVersion
        LookupOperation lookupCodesetVersion = LookupOperation.newLookup()
                .from("codesetVersion")
                .localField("codeSetVersions.$id")
                .foreignField("_id")
                .as("codesetVersion");
        operations.add(lookupCodesetVersion);

        // Step 3: Unwind CodesetVersion
        UnwindOperation unwindCodesetVersion = Aggregation.unwind("codesetVersion");
        operations.add(unwindCodesetVersion);

        // Step 4: Match on the version using $expr
        MatchOperation matchVersion;
        if (searchCriteria.getVersion() != null) {
            matchVersion = Aggregation.match(Criteria.where("codesetVersion.version").is(searchCriteria.getVersion()));
        } else {
            matchVersion = Aggregation.match(Criteria.where("$expr").is(
                    new Document("$eq", Arrays.asList("$codesetVersion.version", "$latestVersion.version"))
            ));
        }
        operations.add(matchVersion);


        // Step 6: Use projection
        ProjectionOperation projection;

        projection = Aggregation.project()
                .and("name").as("name")
                .and("latestVersion").as("latestStableVersion")
                .and("phinvadsOid").as("identifier")
                .and("codesetVersion.version").as("version.version")
                .and("codesetVersion._id").as("version._id")
                .and("codesetVersion.dateUpdated").as("version.date");
        operations.add(projection);

        // Final Aggregation
        Aggregation finalAggregation = Aggregation.newAggregation(operations);
        AggregationResults<CodesetResponse> finalResults = mongoTemplate.aggregate(finalAggregation, "codeset", CodesetResponse.class);


        CodesetResponse codesetResponse = finalResults.getUniqueMappedResult();
        if (codesetResponse == null) {
            return null;  // No matching Codeset found
        }

        //  Fetch codes array based on getMatch()
        Criteria codeCriteria = Criteria.where("codesetversionId").is(codesetResponse.getVersion().getId());
        if (searchCriteria.getMatch() != null) {
            codeCriteria = codeCriteria.and("value").regex(searchCriteria.getMatch(), "i");
        }

        List<Code> codes = mongoTemplate.find(Query.query(codeCriteria), Code.class);
        List<CodeResponse> codeResponses = codes.stream()
                .map(code -> new CodeResponse(code))
                .collect(Collectors.toList());
        codesetResponse.setCodes(codeResponses);
        // Add codeMatchValue only if searchCriteria.getMatch() is provided
        if (searchCriteria.getMatch() != null) {
            codesetResponse.setCodeMatchValue(searchCriteria.getMatch());
        }
        return codesetResponse;
    }

}
