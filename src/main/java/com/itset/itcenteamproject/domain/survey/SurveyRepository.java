package com.itset.itcenteamproject.domain.survey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    //테스트용
    //설문 개수가 아니라 설문 여부를 확인하는 것이기 때문에 count안 쓰고 exists로 사용함
    boolean existsByUserId(Long userId);
}
