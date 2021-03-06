package com.expediagroup.streamplatform.streamregistry.graphql.resolvers;

import java.util.List;
import java.util.Optional;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.expediagroup.streamplatform.streamregistry.graphql.GraphQLApiType;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.ManagedType;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

interface Resolvers {
  // DomainResolver intentionally does not have the GraphQLApiType interface
  // The bean would be wrapped in a metrics proxy and graphql has issues with
  // getting generics from a proxy instance.
  // See https://github.com/graphql-java-kickstart/graphql-java-tools/issues/277
  // TODO upgrade GraphQL and add GraphQLApiType to DomainResolver
  interface DomainResolver extends EntityResolver<Domain>, GraphQLResolver<Domain> {
    List<Schema> schemas(Domain domain);
  }

  interface SchemaResolver extends EntityResolver<Schema>, GraphQLResolver<Schema>, GraphQLApiType {
    Domain domain(Schema schema);
  }

  interface StreamResolver extends EntityResolver<Stream>, GraphQLResolver<Stream>, GraphQLApiType {
    Domain domain(Stream stream);

    Schema schema(Stream stream);
  }

  interface ZoneResolver extends EntityResolver<Zone>, GraphQLResolver<Zone>, GraphQLApiType {}

  interface InfrastructureResolver extends EntityResolver<Infrastructure>, GraphQLResolver<Infrastructure>, GraphQLApiType {
    Zone zone(Infrastructure infrastructure);
  }

  interface StreamBindingResolver extends EntityResolver<StreamBinding>, GraphQLResolver<StreamBinding>, GraphQLApiType {
    Stream stream(StreamBinding streamBinding);

    Infrastructure infrastructure(StreamBinding streamBinding);
  }

  interface ProducerResolver extends EntityResolver<Producer>, GraphQLResolver<Producer>, GraphQLApiType {
    Stream stream(Producer producer);

    Zone zone(Producer producer);

    ProducerBinding binding(Producer producer);
  }

  interface ConsumerResolver extends EntityResolver<Consumer>, GraphQLResolver<Consumer>, GraphQLApiType {
    Stream stream(Consumer consumer);

    Zone zone(Consumer consumer);

    ConsumerBinding binding(Consumer consumer);
  }

  interface ConsumerBindingResolver extends EntityResolver<ConsumerBinding>, GraphQLResolver<ConsumerBinding>, GraphQLApiType {
    Consumer consumer(ConsumerBinding consumerBinding);

    StreamBinding binding(ConsumerBinding consumerBinding);
  }

  interface ProducerBindingResolver extends EntityResolver<ProducerBinding>, GraphQLResolver<ProducerBinding>, GraphQLApiType {
    Producer producer(ProducerBinding producerBinding);

    StreamBinding binding(ProducerBinding producerBinding);
  }

  interface StatusResolver extends GraphQLResolver<Status>, GraphQLApiType {
    ObjectNode getAgentStatus(com.expediagroup.streamplatform.streamregistry.model.Status status);
  }

  interface EntityResolver<E extends ManagedType> {
    default Status status(E entity) {
      return Optional.ofNullable(entity.getStatus()).orElseGet(Status::new);
    }
  }
}
