package cl.pedidos360.ms_rabbitmq_admin.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record CreateQueueRequest(
  @NotBlank(message = "El nombre de la cola es obligatorio.")
  @Size(max = 255)
  @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$")
  String name,
  boolean durable
) {}