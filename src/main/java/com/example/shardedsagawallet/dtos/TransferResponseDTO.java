package com.example.shardedsagawallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDTO {
    private Long sagaInstanceId;
}
