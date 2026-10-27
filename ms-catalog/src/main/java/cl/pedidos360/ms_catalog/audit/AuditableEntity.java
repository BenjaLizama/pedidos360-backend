package cl.pedidos360.ms_catalog.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad base para los documentos que requieren auditoría técnica.
 *
 * <p>Centraliza la información relacionada con la creación y última
 * modificación de un documento, evitando duplicar estos campos en
 * cada entidad del catálogo.</p>
 *
 * <p>Los valores de auditoría son gestionados automáticamente por
 * Spring Data MongoDB mediante {@code @EnableMongoAuditing}.</p>
 *
 * <p>El usuario responsable de cada operación se representa mediante
 * su identificador UUID, obtenido desde el contexto de seguridad.</p>
 */
@Getter
@Setter
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
