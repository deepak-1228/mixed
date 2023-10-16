/**
 * 
 */
package com.stanzaliving.user.controller;

import java.util.Collection;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen.kumar
 *
 * @date 01-Nov-2019
 *
 **/
@Log4j2
@RestController
@RequestMapping("kafka")
public class KafkaManageController {

	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	@GetMapping("/stop/all")
	public ResponseDto<String> stopAll() {

		Collection<MessageListenerContainer> listenerContainers = kafkaListenerEndpointRegistry.getListenerContainers();

		if (CollectionUtils.isNotEmpty(listenerContainers)) {

			log.info("Found " + listenerContainers.size() + " Kafka consumers to stop");

			listenerContainers.forEach(listener -> {

				log.info("Stopping Kafka Consumer: " + listener.getContainerProperties().toString());
				listener.stop();
			});
		} else {
			log.error("No Kafka Consumer found to stop");
		}

		return ResponseDto.success("Stopped " + listenerContainers.size() + " Kafka Consumers");
	}

	@GetMapping("/stop/{consumerId}")
	public ResponseDto<String> stop(@PathVariable String consumerId) {

		MessageListenerContainer listenerContainer = kafkaListenerEndpointRegistry.getListenerContainer(consumerId);

		if (Objects.nonNull(listenerContainer)) {

			log.info("Stopping Kafka Consumer: " + listenerContainer);

			listenerContainer.stop();

			return ResponseDto.success("Stopped Kafka Consumer " + consumerId);
		}

		log.error("No Kafka Consumer found with id: " + consumerId);

		return ResponseDto.failure("No Kafka Consumer found for consumerId: " + consumerId);
	}

	@GetMapping("start/all")
	public ResponseDto<String> startAll() {

		Collection<MessageListenerContainer> listenerContainers = kafkaListenerEndpointRegistry.getListenerContainers();

		if (CollectionUtils.isNotEmpty(listenerContainers)) {

			log.info("Found " + listenerContainers.size() + " Kafka consumers to start");

			listenerContainers.forEach(listener -> {

				log.info("Starting Kafka Consumer: " + listener.getContainerProperties().toString());
				listener.start();
			});

		} else {
			log.error("No Kafka Consumer found to start");
		}

		return ResponseDto.success("Started " + listenerContainers.size() + " Kafka Consumers");
	}

	@GetMapping("/start/{consumerId}")
	public ResponseDto<String> start(@PathVariable String consumerId) {
		MessageListenerContainer listenerContainer = kafkaListenerEndpointRegistry.getListenerContainer(consumerId);

		if (Objects.nonNull(listenerContainer)) {

			log.info("Starting Kafka Consumer: " + listenerContainer);

			listenerContainer.start();

			return ResponseDto.success("Started Kafka Consumer " + consumerId);
		}

		log.error("No Kafka Consumer found with id: " + consumerId);

		return ResponseDto.failure("No Kafka Consumer found for consumerId: " + consumerId);
	}
}