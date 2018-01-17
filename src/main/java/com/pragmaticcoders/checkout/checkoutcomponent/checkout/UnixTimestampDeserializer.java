package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

class UnixTimestampDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        Long unixTimestamp = parser.getLongValue();

        LocalDateTime date = Instant.ofEpochSecond(unixTimestamp)
                .atZone(ZoneId.from(ZoneOffset.UTC))
                .toLocalDateTime();

        return date;

    }
}
