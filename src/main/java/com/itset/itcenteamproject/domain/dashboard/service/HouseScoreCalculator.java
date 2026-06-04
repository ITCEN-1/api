package com.itset.itcenteamproject.domain.dashboard.service;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.house.ContractTypeEnum;
import com.itset.itcenteamproject.domain.house.ContractCntDTO;
import com.itset.itcenteamproject.domain.house.HouseContractRepository;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class HouseScoreCalculator {
    private final Map<String, HouseContractRepository> contractRepositoryMap;
    private final RankingMinMaxNormalizer rankingMinMaxNormalizer;

    public List<RecommendedDong> calcHousePriceScore(Survey survey, List<RecommendedDong> recommendedDongs) {
        List<RecommendedDong> newRecommendedDong = new ArrayList<>();
        List<Integer> recommendedDongCodes = recommendedDongs.stream()
                .map(RecommendedDong::getDongCode)
                .toList();
        List<ContractCntDTO> contractCntDTO = this.contractRepositoryMap.get(getContractType(survey).getBeanName())
                .findContractCntByPreferenceInDongCodes(survey, recommendedDongCodes);

        if (contractCntDTO.isEmpty()) {
            throw new CustomException(ErrorCode.NO_CONTRACT_DATA);
        }

        // 계약 건수 기준 내림차순 정렬하여 rank 부여 (1등이 가장 건수가 많음)
        List<ContractCntDTO> sortedByCnt = contractCntDTO.stream()
                .sorted(Comparator.comparing(ContractCntDTO::getCnt).reversed())
                .collect(Collectors.toList());

        int size = sortedByCnt.size();
        Map<Integer, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < sortedByCnt.size(); i++) {
            rankMap.put(sortedByCnt.get(i).getDongCode(), i + 1); // 1-based rank
        }

        // recommendedDongs에 대해 rank 기반 normalized score를 계산하여 기존 score에 추가
        recommendedDongs.stream()
                .filter(dong -> rankMap.containsKey(dong.getDongCode()))
                .forEach(dong -> {
                    int rank = rankMap.get(dong.getDongCode());
                    BigDecimal normalized = rankingMinMaxNormalizer.getMinMaxNormalizedScore(rank, BigDecimal.valueOf(0), size);
                    BigDecimal existing = dong.getScore() != null ? dong.getScore() : BigDecimal.ZERO;
                    newRecommendedDong.add(
                            RecommendedDong.builder()
                                    .dongCode(dong.getDongCode())
                                    .districtName(dong.getDistrictName())
                                    .dongName(dong.getDongName())
                                    .score(existing.add(normalized))
                                    .longitude(dong.getLongitude())
                                    .latitude(dong.getLatitude())
                                    .message(dong.getMessage() + " house(rank:" + rank + " score:" + normalized + ")")
                                    .build()
                    );
                });

        return newRecommendedDong;
    }

    private ContractTypeEnum getContractType(Survey survey) {
        if (survey.getJeonseMin() != null && survey.getJeonseMax() != null) {
            return ContractTypeEnum.JEONSE;
        }
        return ContractTypeEnum.WOLSE;
    }

}