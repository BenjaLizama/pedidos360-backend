package cl.pedidos360.ms_orders.audit;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Clase base para las entidades JPA que requieren auditoría técnica.
 *
 * <p>Centraliza la información de creación y última modificación
 * de los registros persistidos en PostgreSQL.</p>
 *
 * <p>Spring Data JPA administra automáticamente estos valores
 * mediante AuditingEntityListener y la configuración de auditoría.</p>
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    /**
     * Fecha y hora en que el documento fue creado.
     *
     * <p>Este valor es establecido automáticamente por Spring Data
     * MongoDB y no debe ser modificado manualmente por la lógica
     * de negocio.</p>
     */
    @CreatedDate
    private Instant createdAt;

    /**
     * Identificador UUID del usuario que creó el documento.
     *
     * <p>El valor es obtenido automáticamente mediante la implementación
     * de {@code AuditorAware}.</p>
     */
    @CreatedBy
    private UUID createdBy;

    /**
     * Fecha y hora de la última modificación del documento.
     *
     * <p>Este valor es actualizado automáticamente por Spring Data
     * MongoDB cuando el documento es modificado.</p>
     */
    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Identificador UUID del usuario que realizó la última modificación.
     *
     * <p>El valor es obtenido automáticamente desde el contexto de
     * seguridad mediante {@code AuditorAware}.</p>
     */
    @LastModifiedBy
    private UUID updatedBy;
}
