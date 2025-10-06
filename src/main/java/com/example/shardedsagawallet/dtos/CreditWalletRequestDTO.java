package com.example.shardedsagawallet.dtos;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditWalletRequestDTO {
    private BigDecimal amount;
}
