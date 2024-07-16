package com.example.employee;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, String> {
    @Override
    public String convertToDatabaseColumn(Money attribute) {
        if (attribute == null) return null;
        return attribute.toString();
    }

    @Override
    public Money convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return Money.parse(dbData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
