package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.house.ContractTypeEnum;
import com.itset.itcenteamproject.domain.house.ContractCntDTO;
import com.itset.itcenteamproject.domain.house.HouseContractRepository;
import com.itset.itcenteamproject.domain.survey.Survey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class HouseScoreCalculator {
    private final Map<String, HouseContractRepository> wolseRepositoryMap;

    HouseScoreCalculator(Map<String, HouseContractRepository> wolseRepositoryMap) {
        this.wolseRepositoryMap = wolseRepositoryMap;
    }

    public List<RecommendedDong> calcHousePriceScore(Survey survey, List<RecommendedDong> recommendedDongs) {
        List<ContractCntDTO> contractCntDTO = this.wolseRepositoryMap.get(getContractType(survey).getBeanName())
                .findContractCntByPreference(survey);

        Long maxCnt = contractCntDTO.getFirst().getCnt();

        Map<Integer, Long> cntMap = contractCntDTO.stream()
                .collect(Collectors.toMap(
                        ContractCntDTO::getDongCode,
                        ContractCntDTO::getCnt
                ));

        recommendedDongs.stream()
                .filter(dong -> cntMap.containsKey(dong.getDongCode()))
                .forEach(dong -> {
                    Long curCnt = cntMap.get(dong.getDongCode());
                    double additionalScore = calcScore(maxCnt, curCnt);
                    dong.setScore(dong.getScore().add(BigDecimal.valueOf(additionalScore)));
                });

        return recommendedDongs;
    }

    private ContractTypeEnum getContractType(Survey survey) {
        if (survey.getJeonseMin() != null && survey.getJeonseMax() != null) {
            return ContractTypeEnum.JEONSE;
        }
        return ContractTypeEnum.WOLSE;
    }

    private Double calcScore(Long maxContractCnt, Long curContractCnt) {
        return (double) curContractCnt / maxContractCnt * 100;
    }
}