package org.nikolait.assigment.userdeposit.service.impl;

import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.elastic.EmailDataEs;
import org.nikolait.assigment.userdeposit.elastic.PhoneDataEs;
import org.nikolait.assigment.userdeposit.elastic.UserEs;
import org.nikolait.assigment.userdeposit.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations operations;

    @Override
    public Page<UserEs> searchUsers(String name, String email, String phone, LocalDate dateOfBirth, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    // Поиск по префиксу для name (аналог LIKE 'name%')
                    if (hasText(name)) {
                        b.must(m -> m.prefix(p -> p.field("name").value(name)));
                    }

                    // Поиск по точному совпадению email
                    if (hasText(email)) {
                        b.must(m -> m.nested(n -> n
                                .path("emails")
                                .query(nq -> nq.term(t -> t.field("emails.email").value(email)))
                        ));
                    }
                    // Поиск по точному совпадению phone
                    if (hasText(phone)) {
                        b.must(m -> m.nested(n -> n
                                .path("phones")
                                .query(nq -> nq.term(t -> t.field("phones.phone").value(phone)))
                        ));
                    }

                    // Фильтр по >= dateOfBirth
                    if (nonNull(dateOfBirth)) {
                        b.filter(f -> f.range(r -> r.date(d ->
                                d.field("dateOfBirth").gte(dateOfBirth.toString())
                        )));
                    }
                    return b;
                }))
                .withSourceFilter(new FetchSourceFilterBuilder()
                        .withIncludes("id", "name", "dateOfBirth")
                        .build())
                .withPageable(pageable)
                .build();

        SearchHits<UserEs> searchHits = operations.search(query, UserEs.class);

        return new PageImpl<>(
                searchHits.stream().map(SearchHit::getContent).toList(),
                pageable,
                searchHits.getTotalHits()
        );
    }

    /**
     * Для упрощения возвращаем null, без обработки ошибки
     */
    @Override
    public UserEs getUserBasic(Long userId) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                        .field("id")
                        .value(userId)))
                .withSourceFilter(new FetchSourceFilterBuilder()
                        .withIncludes("id", "name", "dateOfBirth")
                        .build())
                .build();

        SearchHits<UserEs> resp = operations.search(query, UserEs.class);

        return resp.getSearchHits().stream()
                .map(SearchHit::getContent)
                .findFirst()
                .orElse(null);
    }

    /**
     * Для упрощения возвращаем null, без обработки ошибки
     */
    @Override
    public UserEs getUserFull(Long userId) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                        .field("id")
                        .value(userId)))
                .build();

        SearchHits<UserEs> resp = operations.search(query, UserEs.class);

        return resp.getSearchHits().stream()
                .map(SearchHit::getContent)
                .findFirst()
                .orElse(null);
    }

    /**
     * Для упрощения возвращаем пустой список, когда User не найден, без обработки ошибки
     */
    @Override
    public List<EmailDataEs> getUserEmails(Long userId) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                        .field("id")
                        .value(userId)))
                .withSourceFilter(new FetchSourceFilterBuilder()
                        .withIncludes("emails")
                        .build())
                .build();

        SearchHits<UserEs> resp = operations.search(query, UserEs.class);

        return resp.getSearchHits().stream()
                .map(SearchHit::getContent)
                .findFirst()
                .map(UserEs::getEmails)
                .orElse(Collections.emptyList());
    }

    /**
     * Для упрощения возвращаем пустой список, когда User не найден, без обработки ошибки
     */
    @Override
    public List<PhoneDataEs> getUserPhones(Long userId) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                        .field("id")
                        .value(userId)))
                .withSourceFilter(new FetchSourceFilterBuilder()
                        .withIncludes("phones")
                        .build())
                .build();

        SearchHits<UserEs> resp = operations.search(query, UserEs.class);

        return resp.getSearchHits().stream()
                .map(SearchHit::getContent)
                .findFirst()
                .map(UserEs::getPhones)
                .orElse(Collections.emptyList());
    }
}
