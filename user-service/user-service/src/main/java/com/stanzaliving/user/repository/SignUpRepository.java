package com.stanzaliving.user.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.SignupEntity;

@Repository
public interface SignUpRepository extends AbstractJpaRepository<SignupEntity, Long> {

}
