package com.itset.itcenteamproject.domain.house;

import com.itset.itcenteamproject.domain.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WolseRepository extends JpaRepository<Wolse, Long>, HouseContractRepository {
    @Override
    @Query("select new com.itset.itcenteamproject.domain.house.ContractCntDTO(w.dongCode, count(w)) from Wolse as w" +
            " where w.monthlyRent Between #{#survey.monthlyMin} and #{#survey.monthlyMax}" +
            " and w.deposit Between #{#survey.depositMin} and #{#survey.depositMax}" +
            " group by w.dongCode order by count(w) desc")
    List<ContractCntDTO> findContractCntByPreference(Survey survey);
}