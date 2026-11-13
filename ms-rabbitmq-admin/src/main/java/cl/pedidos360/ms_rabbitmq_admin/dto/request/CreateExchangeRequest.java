package cl.pedidos360.ms_rabbitmq_admin.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record CreateExchangeRequest(
  @NotBlank(message = "El nombre del exchange es obligatorio.")
  @Size(max = 255)
  @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$")
  String name,
  @NotBlank(message = "El tipo de exchange es obligatorio.")
  @Pattern(regexp = "(?i)^(direct|topic|fanout|headers)$")
  String type,
  boolean durable
) {}