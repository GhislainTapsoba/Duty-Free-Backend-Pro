package com.djbc.dutyfree.domain.mapper;

import com.djbc.dutyfree.domain.dto.response.SommierResponse;
import com.djbc.dutyfree.domain.entity.Sommier;

import java.util.List;
import java.util.stream.Collectors;

public class SommierMapper {
    
    public static SommierResponse toResponse(Sommier sommier) {
        if (sommier == null) {
            return null;
        }
        
        // Ne pas accéder aux relations lazy pour éviter LazyInitializationException
        return SommierResponse.builder()
                .id(sommier.getId())
                .sommierNumber(sommier.getSommierNumber())
                .purchaseOrderId(null)  // On ne charge pas purchaseOrder
                .openingDate(sommier.getOpeningDate())
                .closingDate(sommier.getClosingDate())
                .initialValue(sommier.getInitialValue())
                .currentValue(sommier.getCurrentValue())
                .clearedValue(sommier.getClearedValue())
                .status(sommier.getStatus())
                .notes(sommier.getNotes())
                .alertDate(sommier.getAlertDate())
                .alertSent(sommier.getAlertSent())
                .createdAt(sommier.getCreatedAt())
                .updatedAt(sommier.getUpdatedAt())
                .build();
    }
    
    public static List<SommierResponse> toResponseList(List<Sommier> sommiers) {
        if (sommiers == null) {
            return List.of();
        }
        
        return sommiers.stream()
                .map(SommierMapper::toResponse)
                .collect(Collectors.toList());
    }
}