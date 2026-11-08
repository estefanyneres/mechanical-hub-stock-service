package com.fiap.mechanical_hub.stock_service.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mesma exchange do Saga que o core-service declara (nome/tipo/durabilidade
 * tem que ser idênticos entre os serviços -- o RabbitMQ rejeita uma segunda
 * declaração da "mesma" exchange com propriedades diferentes).
 *
 * Contrato (ver RabbitMQConfig do core-service pro contrato completo):
 *   stock-service CONSOME (fila stock-service.eventos.queue):
 *     - "execucao.revertida"   {serviceOrderId}  (core-service, TASK-028 -- libera TODO
 *                                                  o material reservado pra essa OS, ja
 *                                                  que o diagnostico comecou do zero)
 *   stock-service PUBLICA:
 *     - "estoque.reabastecido" {serviceOrderId}  (TASK-028/029 -- quando uma pendencia
 *                                                  de uma OS e totalmente resolvida por
 *                                                  uma entrada de estoque. O repair-service
 *                                                  escuta isso pra tentar reservar de novo)
 */
@Configuration
public class RabbitMQConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String STOCK_SERVICE_QUEUE = "stock-service.eventos.queue";

    public static final String ROUTING_KEY_EXECUCAO_REVERTIDA = "execucao.revertida";
    public static final String ROUTING_KEY_ESTOQUE_REABASTECIDO = "estoque.reabastecido";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE, true, false);
    }

    @Bean
    public Queue stockServiceQueue() {
        return new Queue(STOCK_SERVICE_QUEUE, true);
    }

    @Bean
    public Binding execucaoRevertidaBinding(Queue stockServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(stockServiceQueue).to(sagaExchange).with(ROUTING_KEY_EXECUCAO_REVERTIDA);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
