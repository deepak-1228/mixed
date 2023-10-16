/**
 * 
 */
package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.ApiEntity;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
public interface ApiDbService extends AbstractJpaService<ApiEntity, Long> {

	boolean existsByActionUrl(String actionUrl);

	boolean existsByApiName(String apiName);
}