package org.apereo.portal.fbms.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends CrudRepository<SubmissionEntity,SubmissionIdentifier> {

    @Query("SELECT s FROM SubmissionEntity s " +
            "WHERE s.id.timestamp = (SELECT MAX(b.id.timestamp) from SubmissionEntity b WHERE b.id.username = :username AND b.id.fname = :fname) " +
            "AND s.id.username = :username " +
            "AND s.id.fname = :fname")
    SubmissionEntity findMostRecentByUsernameAndFname(@Param("username") String username, @Param("fname") String fname);

}