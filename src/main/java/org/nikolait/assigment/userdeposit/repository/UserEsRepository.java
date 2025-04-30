package org.nikolait.assigment.userdeposit.repository;

import org.nikolait.assigment.userdeposit.elastic.UserEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserEsRepository extends ElasticsearchRepository<UserEs, Long> {
}
