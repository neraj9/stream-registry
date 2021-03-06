/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import static com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventUtils.getWarningMessageOnNotDefinedProp;

import java.util.Objects;
import java.util.Optional;

import org.apache.avro.specific.SpecificRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.expediagroup.streamplatform.streamregistry.core.events.config.NewTopicProperties;
import com.expediagroup.streamplatform.streamregistry.core.events.config.NotificationEventConfig;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.SchemaEventHandlerForKafka;
import com.expediagroup.streamplatform.streamregistry.core.events.handlers.StreamEventHandlerForKafka;

@RunWith(SpringRunner.class)// Not defined prop!
@SpringBootTest(classes = EventHandlerAvoidLoadingWithoutPropTest.MockListenerConfiguration.class)
public class EventHandlerAvoidLoadingWithoutPropTest {

  @Autowired(required = false)
  private Optional<SchemaEventHandlerForKafka> schemaEventHandlerForKafka;

  @Autowired(required = false)
  private Optional<StreamEventHandlerForKafka> streamEventHandlerForKafka;

  @Test
  public void having_notifications_disabled_verify_that_KafkaNotificationEventListener_is_not_being_loaded() {
    Assert.assertNotNull("Optional container of SchemaEventHandlerForKafka shouldn't be null!", schemaEventHandlerForKafka);
    Assert.assertFalse("Kafka schema event handler should NOT be loaded since notification.events.kafka.enabled == false", schemaEventHandlerForKafka.isPresent());
    Assert.assertNotNull("Optional container of StreamEventHandlerForKafka shouldn't be null!", streamEventHandlerForKafka);
    Assert.assertFalse("Kafka stream event handler should NOT be loaded since notification.events.kafka.enabled == false", streamEventHandlerForKafka.isPresent());
  }

  public static class MockListenerConfiguration extends NotificationEventConfig {
    @Value("${" + KAFKA_TOPIC_NAME_PROPERTY + ":#{null}}")
    private String notificationEventsTopic;

    @Value("${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + ":#{null}}")
    private String bootstrapServers;

    @Bean(initMethod = "setup")
    @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
    public KafkaSetupHandler kafkaSetupHandler(NewTopicProperties newTopicProperties) {
      Objects.requireNonNull(notificationEventsTopic, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_TOPIC_NAME_PROPERTY));
      Objects.requireNonNull(bootstrapServers, getWarningMessageOnNotDefinedProp("enabled notification events", KAFKA_BOOTSTRAP_SERVERS_PROPERTY));

      return Mockito.mock(KafkaSetupHandler.class);
    }

    @Bean(name = "producerFactory")
    @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
    public ProducerFactory<SpecificRecord, SpecificRecord> producerFactory() {
      return Mockito.mock(ProducerFactory.class);
    }

    @Bean(name = "kafkaTemplate")
    @ConditionalOnProperty(name = KAFKA_NOTIFICATIONS_ENABLED_PROPERTY)
    public KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate() {
      return Mockito.mock(KafkaTemplate.class);
    }
  }
}