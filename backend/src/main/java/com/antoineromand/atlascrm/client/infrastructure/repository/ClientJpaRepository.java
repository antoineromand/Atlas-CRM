package com.antoineromand.atlascrm.client.infrastructure.repository;

import com.antoineromand.atlascrm.client.infrastructure.model.ClientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
  @EntityGraph(attributePaths = "contacts")
  Optional<ClientEntity> findByIdAndAccount_Id(UUID clientId, UUID accountId);

  @EntityGraph(attributePaths = "contacts")
  Page<ClientEntity> findAllByAccount_Id(UUID accountId, Pageable pageable);

  @EntityGraph(attributePaths = "contacts")
  @Query(
      value =
          """
          select distinct c
          from ClientEntity c
          left join c.contacts contact
          where c.account.id = :accountId
            and (:status is null or c.status = :status)
            and (
              :search is null or :search = '' or
              lower(c.companyName) like lower(concat('%', :search, '%')) or
              lower(coalesce(contact.firstName, '')) like lower(concat('%', :search, '%')) or
              lower(coalesce(contact.lastName, '')) like lower(concat('%', :search, '%')) or
              lower(concat(coalesce(contact.firstName, ''), ' ', coalesce(contact.lastName, ''))) like lower(concat('%', :search, '%'))
            )
          order by c.createdAt desc
          """,
      countQuery =
          """
          select count(distinct c.id)
          from ClientEntity c
          left join c.contacts contact
          where c.account.id = :accountId
            and (:status is null or c.status = :status)
            and (
              :search is null or :search = '' or
              lower(c.companyName) like lower(concat('%', :search, '%')) or
              lower(coalesce(contact.firstName, '')) like lower(concat('%', :search, '%')) or
              lower(coalesce(contact.lastName, '')) like lower(concat('%', :search, '%')) or
              lower(concat(coalesce(contact.firstName, ''), ' ', coalesce(contact.lastName, ''))) like lower(concat('%', :search, '%'))
            )
          """)
  Page<ClientEntity> findAllByAccountIdAndSearch(
      @Param("accountId") UUID accountId,
      @Param("search") String search,
      @Param("status") String status,
      Pageable pageable);
}
