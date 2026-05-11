package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.house.ContractTypeEnum;
import com.itset.itcenteamproject.domain.house.ContractCntDTO;
import com.itset.itcenteamproject.domain.house.HouseContractRepository;
import com.itset.itcenteamproject.domain.survey.Survey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class HouseScoreCalculator {
    private final Map<String, HouseContractRepository> contractRepositoryMap;

    HouseScoreCalculator(Map<String, HouseContractRepository> contractRepositoryMap) {
        this.contractRepositoryMap = contractRepositoryMap;
    }

    public List<RecommendedDong> calcHousePriceScore(Survey survey, List<RecommendedDong> recommendedDongs) {
        List<RecommendedDong> newRecommendedDong = new ArrayList<>();
        List<ContractCntDTO> contractCntDTO = this.contractRepositoryMap.get(getContractType(survey).getBeanName())
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
                    newRecommendedDong.add(
                            RecommendedDong.builder()
                                    .dongCode(dong.getDongCode())
                                    .dongName(dong.getDongName())
                                    .score(BigDecimal.valueOf(additionalScore))
                                    .build()
                    );
                });

        return getTop10RecommendedDongs(newRecommendedDong);
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

    private List<RecommendedDong> getTop10RecommendedDongs(List<RecommendedDong> recommendedDongs) {
        List<RecommendedDong> top10RecommendedDongs = new ArrayList<>();

        return recommendedDongs.stream()
                .sorted(Comparator.comparing(RecommendedDong::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
}