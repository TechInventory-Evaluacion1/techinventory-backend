# TechInventory — microservicios desacoplados

Dos Spring Boot independientes que comparten MySQL en AWS RDS (`inventory_db`).

| Servicio | Puerto | Autenticación | Ruta |
|---|---|---|---|
| `catalogo-service` | 8081 | Ninguna (público) | `/api/productos` y `/api/v1/productos` |
| `inventario-service` | 8082 | Azure AD JWT (`api.read` / `api.write`) | `/api/inventario` y `/api/v1/inventario` |

## API Gateway (AWS)

Frontend: `https://lk7j2r33td.execute-api.us-east-1.amazonaws.com`

Integraciones HTTP sugeridas:

- `ANY /api/productos` y `ANY /api/productos/{proxy+}` → `http://<ec2-catalogo>:8081`
- `ANY /api/inventario` y `ANY /api/inventario/{proxy+}` → `http://<ec2-inventario>:8082`

## Cómo levantar en local

```bash
cd catalogo-service && ./mvnw spring-boot:run
cd inventario-service && ./mvnw spring-boot:run
```
