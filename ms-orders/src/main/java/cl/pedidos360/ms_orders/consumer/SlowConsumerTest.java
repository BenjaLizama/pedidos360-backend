package cl.pedidos360.ms_orders.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SlowConsumerTest {

    @KafkaListener(topics = "sensor-topic", groupId = "analytics-group")
    public void listen(String message) {
        // Simulamos un procesamiento pesado (ej. escribir en BD, IA)
        try {
            Thread.sleep(10); // 10 ms por mensaje = max 100 mensajes/segundo
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
