package org.epos.configuration.repositories;

import org.epos.api.clienthelpers.model.CacheData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CacheDataRepository extends CrudRepository<CacheData, String> {
    
}
