package gov.nist.hit.hl7.codeset.adapter.repository;

import gov.nist.hit.hl7.codeset.adapter.model.Codeset;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodesetRepository extends MongoRepository<Codeset, String> {

    @Query(value = "{ '_id._id' : ?0 }")
    List<Codeset> findByUsername(String username);

    @Query(value = "{ $and :  [{ 'audience.type' : 'PRIVATE' }, { 'audience.editor' : ?0 }] }")
    List<Codeset> findByPrivateAudienceEditor(String username);

    @Query(value = "{ $and :  [{ 'audience.type' : 'PRIVATE' }, { 'audience.viewers' : ?0 }] }")
    List<Codeset> findByPrivateAudienceViewer(String username);
    //
    @Query(value = "{ 'audience.type' : 'PUBLIC'  }")
    List<Codeset> findByPublicAudienceAndStatusPublished();

    @Query(value = "{ 'audience.type' : 'PRIVATE'}")
    List<Codeset> findAllPrivateCodeSet();


}