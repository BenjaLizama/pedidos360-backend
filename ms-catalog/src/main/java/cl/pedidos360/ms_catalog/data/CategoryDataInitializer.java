package cl.pedidos360.ms_catalog.data;

import cl.pedidos360.ms_catalog.entity.CategoryEntity;
import cl.pedidos360.ms_catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Inicializador de datos base para el catálogo.
 *
 * <p>Registra las categorías iniciales requeridas por el sistema
 * al iniciar la aplicación.</p>
 *
 * <p>La carga es idempotente, por lo que las categorías que ya
 * existen no vuelven a ser creadas.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    /**
     * Ejecuta la carga de categorías iniciales al iniciar
     * la aplicación.
     *
     * @param args argumentos entregados durante el inicio
     *             de la aplicación
     */
    @Override
    public void run(String @NonNull ... args) throws Exception {
        List<CategorySeed> categories = List.of(
                new CategorySeed(
                        "OTROS",
                        "Productos sin categoria definida."
                )
        );

        categories.forEach(this::createIfNotExists);
    }

    /**
     * Crea una categoría inicial únicamente si no existe
     * una categoría con el mismo nombre.
     *
     * @param seed datos base de la categoría a crear
     */
    private void createIfNotExists(CategorySeed seed) {
        if (categoryRepository.existsByNameIgnoreCase(seed.name())) {
            log.debug(
                    "Categoría inicial ya existente | name={}",
                    seed.name()
            );

            return;
        }

        CategoryEntity category = new CategoryEntity();
        category.setName(seed.name().toUpperCase());
        category.setDescription(seed.description());
        category.setActive(true);

        categoryRepository.save(category);

        log.info(
                "Categoría inicial creada | name={}",
                seed.name()
        );
    }

    /**
     * Representa los datos necesarios para registrar
     * una categoría inicial.
     *
     * @param name nombre de la categoría
     * @param description descripción de la categoría
     */
    private record CategorySeed(
       String name,
       String description
    ) {}
}
