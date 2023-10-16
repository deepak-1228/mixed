/**
 * 
 */
package com.stanzaliving.user.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.UserPropertyMappingEntity;

/**
 * @author naveen
 *
 * @date 13-Oct-2019
 */
@Repository
public interface UserPropertyMappingRepository extends AbstractJpaRepository<UserPropertyMappingEntity, Long> {

	List<UserPropertyMappingEntity> findByUserId(String userId, Pageable pageable);

	List<UserPropertyMappingEntity> findByUserIdAndStatus(String userId, boolean status, Pageable pageable);

	boolean existsByUserIdAndPropertyId(String userId, String propertyId);
}