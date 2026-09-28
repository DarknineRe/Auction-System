package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.domain.entity.BidAction;
import com.example.project.dto.response.BidActionResponse;

@Component
public class BidActionMapper {

    public BidActionResponse toResponse(BidAction action) {
        return new BidActionResponse(
                action.getId(),
                action.getBidding().getId(),
                action.getUser().getId(),
                action.getAmount(),
                action.getTimestamp());
    }
}