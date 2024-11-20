package com.example.employee;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;

@Embeddable
public record Money(
        BigDecimal amount,
        Currency currency
) {
    private static final String DEFAULT_PATTERN = "#,##0.00";

    @Override
    public String toString() {

        DecimalFormat decimalFormat = new DecimalFormat(DEFAULT_PATTERN);
        var amountPart = decimalFormat.format(amount);
        var currencyPart = currency.getCurrencyCode();
        return amountPart + " " + currencyPart;
    }

    public static Money parse(String value) throws Exception {
        var parts = value.split(" ");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(DEFAULT_PATTERN, symbols);
        decimalFormat.setParseBigDecimal(true);

        var amount = (BigDecimal) decimalFormat.parse(parts[0]);
        var currency = Currency.getInstance(parts[1]);
        return new Money(amount, currency);
    }
}
