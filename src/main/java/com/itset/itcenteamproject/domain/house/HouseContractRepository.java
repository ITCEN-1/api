package com.itset.itcenteamproject.domain.house;

import com.itset.itcenteamproject.domain.survey.entity.Survey;

import java.util.List;

public interface HouseContractRepository {
    List<ContractCntDTO> findContractCntByPreferenceInDongCodes(Survey survey, List<Integer> dongCodes);
}