# TechInventory — Backend de Microservicios (Arquitectura Híbrida Cloud & Eventos)

Ecosistema backend de **TechInventory (E-Tech Logistics)** desarrollado con **Java 21** y **Spring Boot 3.2.3**, compuesto por dos microservicios desacoplados que combinan despliegue en nube (**AWS EC2, RDS MySQL, API Gateway**), seguridad OAuth2 con **Microsoft Entra ID**, mensajería transaccional con **Clúster RabbitMQ** y streaming/analítica en tiempo real con **Apache Kafka**.

---

## Evolución de la Arquitectura (Entregas 1 a 3)

### Entrega 1 — Microservicios Base, Cloud AWS y Seguridad JWT
* Desacoplamiento en dos servicios Spring Boot independientes conectados a **MySQL** (`techinventory` en local / `inventory_db` en **AWS RDS**):
  * **`catalogo-service` (Puerto `8081`):** API pública para consulta, creación y reserva de productos.
  * **`inventario-service` (Puerto `8082`):** API protegida como *OAuth2 Resource Server* mediante tokens JWT de **Microsoft Entra ID (Azure AD)** con validación de scopes (`api.read` / `api.write`).
* Exposición y enrutamiento centralizado mediante **AWS API Gateway** hacia instancias **AWS EC2**.

### Entrega 2 — Mensajería Transaccional Asíncrona (Clúster RabbitMQ)
* Despliegue de un **clúster multi-nodo de RabbitMQ** (`rabbitmq1` y `rabbitmq2`) en Docker Compose compartiendo cookie de Erlang.
* Configuración centralizada en `application.properties` (sin nombres fijos en código) con exchanges **`Direct`**, **`Topic`** (`inventario.stock.#`) y **`DLX`** (`techinventory.dlx.exchange`).
* Implementación de **3 colas durables con sus respectivas Dead Letter Queues (DLQ)**:
  * `inventario.producto.crear.queue` → `inventario.producto.crear.dlq`
  * `inventario.stock.queue` → `inventario.stock.dlq`
  * `inventario.notificacion.queue` → `inventario.notificacion.dlq`
* Consumidores con confirmación manual explícita (**`ACK` / `NACK` sin reencolado infinito**) y **API REST de administración** (`/api/v1/admin/...`).

### Entrega 3 — Streaming y Analítica en Tiempo Real (Apache Kafka)
* Incorporación de **Zookeeper**, **Apache Kafka** (`confluentinc/cp-kafka:7.5.0`) y consola web **Kafka-UI** (`http://localhost:8085`) en Docker Compose con `KAFKA_AUTO_CREATE_TOPICS_ENABLE=false`.
* Arquitectura híbrida en `CatalogoService`: publica simultáneamente eventos transaccionales en **RabbitMQ** y eventos de streaming/telemetría en **Apache Kafka** (`acks=all`).
* Definición controlada de **2 tópicos especializados**:
  * `techinventory.catalogo.eventos` (3 particiones, retención 7 días, política `delete`).
  * `techinventory.inventario.analitica` (2 particiones, retención 30 días, política `compact,delete`).
* Dos **Consumer Groups independientes** en `InventarioKafkaConsumer` (`inventario-catalogo-group` e `inventario-analitica-group`) con `AckMode.RECORD`, manejo de errores mediante `DefaultErrorHandler` + `FixedBackOff` y alertas automáticas de stock crítico (`<= 5` unidades).
* **API REST de Administración Kafka** con `AdminClient` (`/api/v1/kafka/admin/...`).

---

## Servicios y Puertos del Ecosistema

| Componente / Servicio | Puerto Local | Tecnología / Rol | Seguridad / Acceso |
| :--- | :---: | :--- | :--- |
| **`catalogo-service`** | `8081` | Spring Boot 3.2.3 (Productor RabbitMQ & Kafka + Admin APIs) | Público |
| **`inventario-service`** | `8082` | Spring Boot 3.2.3 (Consumidor RabbitMQ & Kafka + Gestión Stock) | Azure AD JWT (`api.read` / `api.write`) |
| **MySQL Local (Docker)** | `3307` | Base de datos relacional (`techinventory`) | Usuario `root` / `root` |
| **RabbitMQ Nodo 1 (`rabbit1`)** | `5672` / `15672` | Broker AMQP + Management UI | `guest` / `guest` |
| **RabbitMQ Nodo 2 (`rabbit2`)** | `5673` / `15673` | Nodo secundario del clúster RabbitMQ | `guest` / `guest` |
| **Zookeeper** | `2181` | Coordinación del clúster Kafka | Interno / Host |
| **Apache Kafka Broker** | `9092` / `29092` | Streaming de eventos y analítica | `PLAINTEXT` |
| **Kafka-UI** | `8085` | Interfaz web de monitoreo de tópicos, particiones y grupos | Web (`http://localhost:8085`) |

---

## Endpoints Principales

### 1. Negocio (Catálogo e Inventario)
| Método | Ruta | Servicio | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/productos` | `catalogo-service` | Lista todos los productos disponibles en catálogo. |
| `POST` | `/api/productos` | `catalogo-service` | Crea un producto en BD y dispara eventos a RabbitMQ y Kafka. |
| `POST` | `/api/productos/reservar` | `catalogo-service` | Reserva stock y publica evento `STOCK_RESERVADO` en Kafka. |
| `GET` | `/api/v1/inventario` | `inventario-service` | Obtiene el inventario completo (requiere token JWT). |
| `POST` | `/api/v1/inventario` | `inventario-service` | Registra un producto directamente en inventario. |
| `PUT/PATCH` | `/api/v1/inventario/{id}/stock` | `inventario-service` | Actualiza unidades de stock de un producto. |
| `DELETE` | `/api/v1/inventario/{id}` | `inventario-service` | Elimina un producto del inventario (`204 No Content`). |

### 2. Administración Dinámica de RabbitMQ (`catalogo-service` — `/api/v1/admin`)
* `GET /api/v1/admin/queues` — Lista las colas registradas.
* `POST /api/v1/admin/queues` — Crea una nueva cola durable con soporte DLX.
* `DELETE /api/v1/admin/queues/{name}` — Elimina una cola específica.
* `POST /api/v1/admin/exchanges` — Crea un Exchange (`Direct` o `Topic`).
* `POST /api/v1/admin/bindings` — Asocia una cola a un exchange mediante routing key.

### 3. Administración Dinámica de Apache Kafka (`catalogo-service` — `/api/v1/kafka/admin`)
* `GET /api/v1/kafka/admin/topics` — Lista tópicos activos, particiones y réplicas.
* `POST /api/v1/kafka/admin/topics` — Crea un tópico con particiones y configuraciones.
* `GET /api/v1/kafka/admin/topics/{nombre}/configs` — Consulta políticas (`cleanup.policy`, `retention.ms`).
* `PUT /api/v1/kafka/admin/topics/{nombre}/configs` — Modifica propiedades del tópico en caliente.
* `PUT /api/v1/kafka/admin/topics/{nombre}/partitions/{total}` — Escala el número de particiones.
* `DELETE /api/v1/kafka/admin/topics/{nombre}` — Elimina un tópico.
* `GET /api/v1/kafka/admin/consumer-groups` — Inspecciona Consumer Groups activos y su estado (`STABLE`).

---

## Guía de Ejecución en Local

### Paso 1: Levantar Base de Datos MySQL (si no está creada)
```bash
docker run --name mysql-tech -p 3307:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=techinventory -d mysql:8.0
```

### Paso 2: Levantar Clúster RabbitMQ, Zookeeper, Kafka y Kafka-UI
Desde la raíz del repositorio (`techinventory-backend`):
```bash
docker compose up -d
docker ps
```

### Paso 3: Iniciar los Microservicios Spring Boot
En dos terminales independientes:

**Terminal 1 — `catalogo-service` (Puerto 8081):**
```bash
cd catalogo-service
./mvnw spring-boot:run
```

**Terminal 2 — `inventario-service` (Puerto 8082):**
```bash
cd inventario-service
./mvnw spring-boot:run
```

### Paso 4: Consolas de Monitoreo Local
* **RabbitMQ Management UI:** [http://localhost:15672](http://localhost:15672) (`guest` / `guest`)
* **Kafka-UI:** [http://localhost:8085](http://localhost:8085)

---

## Despliegue Cloud (AWS — Entrega 1)

Para ejecución en entorno **AWS Academy Learner Lab**, cada microservicio cuenta con la configuración comentada en `application.properties` para apuntar a **AWS RDS MySQL** (`inventory_db`) y enrutarse a través de **AWS API Gateway**:
* `ANY /api/productos` y `ANY /api/productos/{proxy+}` → `http://<ec2-catalogo>:8081`
* `ANY /api/inventario` y `ANY /api/inventario/{proxy+}` → `http://<ec2-inventario>:8082`

---

## Repositorio Oficial

* **Backend:** [https://github.com/TechInventory-Evaluacion1/techinventory-backend](https://github.com/TechInventory-Evaluacion1/techinventory-backend)