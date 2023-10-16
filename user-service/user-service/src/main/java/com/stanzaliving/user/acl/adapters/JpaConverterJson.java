package com.stanzaliving.user.acl.adapters;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;

import lombok.extern.log4j.Log4j2;

@Converter(autoApply = true)
@Log4j2
public class JpaConverterJson implements AttributeConverter<Object, String> {
	private final static ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(Object meta) {
		try {
			return objectMapper.writeValueAsString(meta);
		} catch (JsonProcessingException ex) {
			log.error("Unexpected IOEx incoding json for database: ", ex);
			return null;

		}
	}

	@Override
	public AddUserRequestDto convertToEntityAttribute(String dbData) {
		try {
			return objectMapper.readValue(dbData, AddUserRequestDto.class);
		} catch (IOException ex) {
			log.error("Unexpected IOEx decoding json from database: ", ex);
			return null;
		}
	}
}