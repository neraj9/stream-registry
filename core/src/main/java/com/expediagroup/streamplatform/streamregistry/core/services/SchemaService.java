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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.SchemaValidator;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

@Component
@RequiredArgsConstructor
public class SchemaService {
  private final HandlerService handlerService;
  private final SchemaValidator schemaValidator;
  private final SchemaRepository schemaRepository;
  private final NotificationEventEmitter<Schema> schemaServiceEventEmitter;

  public Optional<Schema> create(Schema schema) throws ValidationException {
    if (schemaRepository.findById(schema.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    schemaValidator.validateForCreate(schema);
    schema.setSpecification(handlerService.handleInsert(schema));
    return schemaServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, schemaRepository.save(schema));
  }

  public Optional<Schema> read(SchemaKey key) {
    return schemaRepository.findById(key);
  }

  public Iterable<Schema> readAll() {
    return schemaRepository.findAll();
  }

  public Optional<Schema> update(Schema schema) throws ValidationException {
    Optional<Schema> existing = schemaRepository.findById(schema.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    schemaValidator.validateForUpdate(schema, existing.get());
    schema.setSpecification(handlerService.handleUpdate(schema, existing.get()));
    return schemaServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, schemaRepository.save(schema));
  }

  public Optional<Schema> upsert(Schema schema) throws ValidationException {
    return !schemaRepository.findById(schema.getKey()).isPresent() ?
        create(schema) :
        update(schema);
  }

  public void delete(Schema schema) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Schema> findAll(Predicate<Schema> filter) {
    return schemaRepository.findAll().stream().filter(filter).collect(toList());
  }

  public List<Schema> find(DomainKey key) {
    Schema example = new Schema(
        new SchemaKey(
            key.getName(),
            null
        ), null, null);
    return schemaRepository.findAll(Example.of(example));
  }

  public void validateSchemaBindingExists(SchemaKey key) {
    if (read(key).isEmpty()) {
      throw new ValidationException("Schema does not exist");
    }
  }
}
