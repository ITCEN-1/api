package com.itset.itcenteamproject.domain.house;

import com.itset.itcenteamproject.domain.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WolseRepository extends JpaRepository<Wolse, Long>, HouseContractRepository {
    @Override
    /*@Query("select new com.itset.itcenteamproject.domain.house.ContractCntDTO(w.dongCode, count(w)) from Wolse as w" +
            " where w.monthlyRent Between #{#survey.monthlyMin} and #{#survey.monthlyMax}" +
            " and w.deposit Between #{#survey.depositMin} and #{#survey.depositMax}" +
            " group by w.dongCode order by count(w) desc")*/
    //WolseRepository의 JPQL 문법 오류
    //#{#survey.monthlyMin} 처럼 썼는데
    //JPQL에서 SpEL 바인딩은 반드시 :#{...} 형태여야 함.
    //# 앞에 :가 빠져서 파서가 깨짐
    @Query("select new com.itset.itcenteamproject.domain.house.ContractCntDTO(w.dongCode, count(w)) " +
            "from Wolse w " +
            "where w.monthlyRent between :#{#survey.monthlyMin} and :#{#survey.monthlyMax} " +
            "and w.deposit between :#{#survey.depositMin} and :#{#survey.depositMax} " +
            "group by w.dongCode order by count(w) desc")
    List<ContractCntDTO> findContractCntByPreference(Survey survey);
}