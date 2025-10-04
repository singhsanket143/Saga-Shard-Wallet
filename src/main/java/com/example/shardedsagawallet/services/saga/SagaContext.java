package com.example.shardedsagawallet.services.saga;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class SagaContext {
    
    private Map<String, Object> data;
    
    public SagaContext(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Long getLong(String key) {
        Object value = get(key);
        if ( value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = get(key);
        if ( value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return null;
    }

    public String getString(String key) {
        Object value = get(key);
        if ( value instanceof String) {
            return (String) value;
        }
        return null;
    }
}
