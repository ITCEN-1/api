package com.itset.itcenteamproject.domain.house;

import com.itset.itcenteamproject.domain.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JeonseRepository extends JpaRepository<Jeonse, Long>, HouseContractRepository {
    @Override
    @Query("select j.dongCode, count(*) as cnt from Jeonse as j" +
            " where j.jeonseRent between #{#survey.jeonseMin} and #{#survey.jeonseMax}" +
            " group by j.dongCode order by cnt desc")
    List<ContractCntDTO> findContractCntByPreference(Survey survey);
}