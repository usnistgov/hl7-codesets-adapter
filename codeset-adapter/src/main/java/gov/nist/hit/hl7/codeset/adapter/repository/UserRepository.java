package gov.nist.hit.hl7.codeset.adapter.repository;

import gov.nist.hit.hl7.codeset.adapter.model.ApplicationUser;
import gov.nist.hit.hl7.codeset.adapter.model.CodesetVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<ApplicationUser, String> {
    ApplicationUser findByUsername(String username);

}