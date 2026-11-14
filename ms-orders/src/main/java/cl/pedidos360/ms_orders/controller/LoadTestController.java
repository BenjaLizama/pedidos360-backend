package cl.pedidos360.ms_orders.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class LoadTestController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/start")
    // @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String startLoadTest(@RequestParam(defaultValue = "10000") int count) {
        long startTime = System.currentTimeMillis();

        // Bucle limpio sin .parallel() para evitar el bloqueo del ForkJoinPool
        for (int i = 0; i < count; i++) {
            String message = "Sensor-Data-" + i + "-Payload-Voluminoso-Para-Llenar-Bytes";

            // Enviar sin esperar respuesta (Fire and Forget para maxima velocidad de prueba)
            kafkaTemplate.send("sensor-topic", "sensor-" + (i % 5), message);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        double throughput = duration > 0 ? count / (duration / 1000.0) : 0.0;

        return String.format(
                "🚀 Carga completada: %d mensajes enviados en %d ms. Throughput: %.2f msg/sec",
                count, duration, throughput
        );
    }
}
