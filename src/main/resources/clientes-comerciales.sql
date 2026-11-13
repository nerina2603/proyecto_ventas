use ventas;

CREATE TABLE IF NOT EXISTS `cliente_has_comercial` (
    `cliente_id` INT UNSIGNED NOT NULL,
    `comercial_id` INT UNSIGNED NOT NULL,
    `fecha` DATE NULL,
    `prioridad` INT NOT NULL DEFAULT 1,
    PRIMARY KEY (`cliente_id`, `comercial_id`),
    INDEX `fk_cliente_has_comercial_comercial1_idx` (`comercial_id` ASC) VISIBLE,
    INDEX `fk_cliente_has_comercial_cliente1_idx` (`cliente_id` ASC) VISIBLE,
    CONSTRAINT `fk_cliente_has_comercial_cliente1` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT `fk_cliente_has_comercial_comercial1` FOREIGN KEY (`comercial_id`) REFERENCES `comercial` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB;