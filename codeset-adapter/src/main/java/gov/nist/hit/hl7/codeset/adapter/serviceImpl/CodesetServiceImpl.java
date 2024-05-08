package gov.nist.hit.hl7.codeset.adapter.serviceImpl;

import gov.nist.hit.hl7.codeset.adapter.model.request.CodesetRequest;
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

    @Override
    public CodesetResponse getCodeset(String provider, String id, CodesetSearchCriteria searchCriteria) throws IOException {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("provider").regex("^" + Pattern.quote(provider) + "$", "i"), // Adjust "someField" to the actual field for provider
                Criteria.where("phinvadsOid").is(id)
        );


        // Manually handle DBRefs: assuming 'codeSetVersions' holds DBRef
        ProjectionOperation projectToExtractIds = Aggregation.project()
                .andExpression("codeSetVersions.$id").as("versionIds")
                .andInclude("phinvadsOid", "name", "latestVersion", "dateUpdated");

        // Lookup to join with the CodesetVersion collection
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("codesetVersion")
                .localField("versionIds")
                .foreignField("_id")
                .as("codeSetVersionsJoined");
        // Unwind the resulting array to handle documents separately
        UnwindOperation unwindOperation = Aggregation.unwind("codeSetVersionsJoined");


        // Conditional match based on version
        MatchOperation matchVersionOperation = Aggregation.match(
                Criteria.where("$expr").is(
                        new org.bson.Document("$or", java.util.Arrays.asList(
                                new org.bson.Document("$eq", java.util.Arrays.asList(
                                        "$codeSetVersionsJoined.version", searchCriteria.getVersion() != null ?
                                                searchCriteria.getVersion() : "$$REMOVE")),
                                new org.bson.Document("$eq", java.util.Arrays.asList("$codeSetVersionsJoined.version",
                                        "$latestVersion.version"))
                        ))
                )
        );
        // Create a custom expression for filtering codes
        AggregationExpression filterExpression = new AggregationExpression() {
            @Override
            public Document toDocument(AggregationOperationContext context) {
                if (searchCriteria.getMatch() == null) {
                    return new Document("$filter", new Document("input", "$codeSetVersionsJoined.codes").append("as", "code")
                            .append("cond", new Document()));
                }
                Document filterCond = new Document("$cond", Arrays.asList(
                        new Document("$eq", Arrays.asList("$$code.pattern", true)),
                        new Document("$regexMatch", new Document("input", "$$code.value").append("regex", searchCriteria.getMatch()).append("options", "i")),
                        new Document("$eq", Arrays.asList("$$code.value", searchCriteria.getMatch()))
                ));
                return new Document("$filter", new Document("input", "$codeSetVersionsJoined.codes")
                        .append("as", "code")
                        .append("cond", filterCond));
            }
        };
        // Projection to shape the output
//        ProjectionOperation projectionOperation = Aggregation.project()
//                .and("name").as("name")
//                .and("latestVersion").as("latestStableVersion")
//                .and(context -> {
//                    return new Document("$ifNull", Arrays.asList(searchCriteria.getMatch(), null));
//                }).as("codeMatchValue")
//                .and("codeSetVersionsJoined.codes").as("codes")
//                .andExpression("{ $map: { input: '$codeSetVersionsJoined.codes', as: 'code', in: { " +
//                        "value: '$$code.value', displayText: '$$code.description', codeSystem: '$$code.codeSystem' } } }"
//                ).as("codes")
//                .and(filterExpression).as("codes");
        ProjectionOperation projectionOperation = Aggregation.project()
                .and("name").as("name")
                .and("latestVersion").as("latestStableVersion")
                .and(context -> {
                    return new Document("$ifNull", Arrays.asList(searchCriteria.getMatch(), null));
                }).as("codeMatchValue")
                .and("codeSetVersionsJoined.codes").as("codes")
                .andExpression("{ $map: { input: '$codeSetVersionsJoined.codes', as: 'code', in: { " +
                        "value: '$$code.value', description: '$$code.description', codeSystem: '$$code.codeSystem' } } }"
                ).as("codes")

                .and(filterExpression).as("codes")
                // Adding the version object
                .and("codeSetVersionsJoined.version").as("version.version")
                .and("codeSetVersionsJoined.dateUpdated").as("version.date");

        if (provider != null && provider.equalsIgnoreCase("phinvads")) {
            projectionOperation = projectionOperation.and("phinvadsOid").as("identifier");
        } else {

        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                projectToExtractIds,
                lookupOperation,
                unwindOperation,
                matchVersionOperation,
                projectionOperation
        );

        // Execute the aggregation
        AggregationResults<CodesetResponse> results = mongoTemplate.aggregate(aggregation, "codeset", CodesetResponse.class);
        System.out.println(provider + id);
        System.out.println(results.getRawResults());

        // Expect only one result
        return results.getUniqueMappedResult();
    }

}
