package cl.pedidos360.ms_rabbitmq_admin.service;
import cl.pedidos360.ms_rabbitmq_admin.dto.request.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import java.util.Locale;
@Service
public class RabbitAdminService {
  private final RabbitAdmin rabbitAdmin;
  public RabbitAdminService(RabbitAdmin rabbitAdmin) { this.rabbitAdmin = rabbitAdmin; }
  public void createQueue(CreateQueueRequest request) {
    rabbitAdmin.declareQueue(new Queue(request.name(), request.durable(), false, false));
  }
  public void deleteQueue(String name) { rabbitAdmin.deleteQueue(name); }
  public void createExchange(CreateExchangeRequest request) {
    Exchange exchange = switch (request.type().toLowerCase(Locale.ROOT)) {
      case "topic" -> new TopicExchange(request.name(), request.durable(), false);
      case "fanout" -> new FanoutExchange(request.name(), request.durable(), false);
      case "headers" -> new HeadersExchange(request.name(), request.durable(), false);
      default -> new DirectExchange(request.name(), request.durable(), false);
    };
    rabbitAdmin.declareExchange(exchange);
  }
  public void deleteExchange(String name) { rabbitAdmin.deleteExchange(name); }
  public void createBinding(CreateBindingRequest request) { rabbitAdmin.declareBinding(toBinding(request)); }
  public void removeBinding(CreateBindingRequest request) { rabbitAdmin.removeBinding(toBinding(request)); }
  private Binding toBinding(CreateBindingRequest request) {
    return new Binding(request.destinationQueue(), Binding.DestinationType.QUEUE, request.exchange(), request.routingKey(), null);
  }
}