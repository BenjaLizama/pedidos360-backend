package cl.pedidos360.ms_rabbitmq_admin.controller;
import cl.pedidos360.ms_rabbitmq_admin.dto.request.*;
import cl.pedidos360.ms_rabbitmq_admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin")
public class RabbitAdminController {
  private final RabbitAdminService adminService;
  public RabbitAdminController(RabbitAdminService adminService) { this.adminService = adminService; }
  @PostMapping("/queues")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> createQueue(@Valid @RequestBody CreateQueueRequest request) {
    adminService.createQueue(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
  @DeleteMapping("/queues/{name}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
    adminService.deleteQueue(name);
    return ResponseEntity.noContent().build();
  }
  @PostMapping("/exchanges")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> createExchange(@Valid @RequestBody CreateExchangeRequest request) {
    adminService.createExchange(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
  @DeleteMapping("/exchanges/{name}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
    adminService.deleteExchange(name);
    return ResponseEntity.noContent().build();
  }
  @PostMapping("/bindings")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> createBinding(@Valid @RequestBody CreateBindingRequest request) {
    adminService.createBinding(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
  @DeleteMapping("/bindings")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> removeBinding(@Valid @RequestBody CreateBindingRequest request) {
    adminService.removeBinding(request);
    return ResponseEntity.noContent().build();
  }
}