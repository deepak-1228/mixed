/**
 * 
 */
package com.stanzaliving.user.acl.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.ApiEntity;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@Repository
public interface ApiRepository extends AbstractJpaRepository<ApiEntity, Long> {

	boolean existsByActionUrl(String actionUrl);

    boolean existsByApiName(String apiName);
}