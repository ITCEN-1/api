package com.itset.itcenteamproject.domain.house;

import com.itset.itcenteamproject.domain.survey.Survey;

import java.util.List;

public interface HouseContractRepository {
    List<ContractCntDTO> findContractCntByPreference(Survey survey);
}