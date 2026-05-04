package com.itset.itcenteamproject.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
/**
 * 1. 이 클래스는 직접적인 테이블 매핑이 되지 않으며, 자식 엔티티에게 매핑 정보(필드)만 제공
 * 2. 부모 클래스 타입으로 조회(em.find)가 불가능하며, 추상 클래스로 작성하는 것을 권장
 * 3. 공통 필드(생성일, 수정일, softdelete 등)를 한곳에서 관리하여 코드 중복을 제거하는 것이 목적
 * ID 전략이 엔티티마다 다를 수 있으므로 ID 필드는 넣지 않았음
 */
@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class})
@SQLRestriction("is_deleted = false") // BaseEntity를 상속한 모든 엔티티의 JPQL/Repository 조회에 WHERE is_deleted = false 조건이 자동으로 붙음
@Getter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @LastModifiedDate
    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updated_at;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // 기본값은 false

    public void softDelete() { // 사용 시 서비스 레이어에서 entity.softDelete() 호출 후 별도 delete 쿼리 없이 변경 감지(dirty checking)로 UPDATE
        this.isDeleted = true;
    }
}