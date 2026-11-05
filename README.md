# CRUD de Produtos com Java 21, Spring Boot e PostgreSQL

API REST para cadastrar, listar, atualizar e remover produtos. O projeto usa Spring Boot 3.5, Spring Data JPA, Bean Validation, Flyway para versionar o banco e PostgreSQL como banco de dados.

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Pré-requisitos](#pré-requisitos)
- [Como rodar](#como-rodar)
- [Configuração](#configuração)
- [Endpoints](#endpoints)
- [Tratamento de erros](#tratamento-de-erros)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Testes](#testes)

## Tecnologias

| Tecnologia | Para que serve no projeto |
|---|---|
| Java 21 | Linguagem (versão LTS), com `record` para os DTOs |
| Spring Boot 3.5 | Configuração automática e servidor embutido (Tomcat) |
| Spring Web | Controllers REST |
| Spring Data JPA / Hibernate | Acesso ao banco sem SQL escrito à mão |
| Bean Validation | Valida o corpo das requisições (`@NotBlank`, `@PositiveOrZero`...) |
| Flyway | Cria e versiona as tabelas a partir de scripts SQL |
| PostgreSQL | Banco de dados relacional |
| Lombok | Gera getters e construtores na entidade |
| JUnit 5, Mockito, MockMvc | Testes unitários e de controller |
| Testcontainers | Sobe um PostgreSQL real em Docker para os testes de integração |

## Arquitetura

O código segue uma divisão em camadas, cada uma com uma responsabilidade:

```
Requisição HTTP
      │
      ▼
ProductController  → recebe a requisição, valida o JSON e define o status HTTP
      │
      ▼
ProductService     → regras de negócio e transações (@Transactional)
      │
      ▼
ProductRepository  → acesso ao banco (Spring Data JPA)
      │
      ▼
PostgreSQL
```

- **DTOs** (`ProductRequest` e `ProductResponse`) separam o que a API recebe e devolve da entidade JPA. Assim o cliente não consegue, por exemplo, enviar um `id` no corpo e a estrutura do banco pode mudar sem quebrar o contrato da API.
- **`GlobalExceptionHandler`** centraliza o tratamento de erros e devolve respostas no padrão [RFC 9457 (Problem Details)](https://www.rfc-editor.org/rfc/rfc9457).
- **Injeção por construtor** em vez de `@Autowired` em campo: as dependências ficam explícitas, imutáveis (`final`) e fáceis de substituir em testes.

## Estrutura de pastas

```
src/
├── main/
│   ├── java/com/example/crud/
│   │   ├── CrudApplication.java            ponto de entrada da aplicação
│   │   ├── controllers/
│   │   │   └── ProductController.java      endpoints REST
│   │   ├── domain/product/
│   │   │   ├── Product.java                entidade JPA
│   │   │   └── ProductRepository.java      repositório Spring Data
│   │   ├── dto/
│   │   │   ├── ProductRequest.java         corpo de entrada (POST/PUT) com validações
│   │   │   └── ProductResponse.java        corpo de saída
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java converte exceções em respostas HTTP
│   │   │   └── ProductNotFoundException.java
│   │   └── service/
│   │       └── ProductService.java         regras de negócio
│   └── resources/
│       ├── application.properties          configurações
│       └── db/migration/                   scripts do Flyway
└── test/                                   testes unitários, de controller e de integração
compose.yaml                                PostgreSQL local via Docker
```

## Pré-requisitos

- **Java 21** ou superior
- **Docker** (recomendado) para subir o PostgreSQL e rodar os testes de integração, ou um PostgreSQL instalado localmente

Não é preciso instalar o Maven: o projeto inclui o Maven Wrapper (`./mvnw`).

## Como rodar

1. Clone o repositório e entre na pasta:

   ```shell
   git clone <url-do-repositorio>
   cd crud-java-spring
   ```

2. Suba o PostgreSQL com Docker:

   ```shell
   docker compose up -d
   ```

   Isso cria o banco `product` com usuário `postgres` e senha `postgres` na porta `5432`, que são os valores padrão da aplicação.

3. Inicie a aplicação:

   ```shell
   ./mvnw spring-boot:run
   ```

   No Windows, use `mvnw.cmd spring-boot:run`.

4. A API fica disponível em `http://localhost:8080`. Na primeira execução o Flyway cria a tabela `product` automaticamente.

Para gerar um `.jar` executável:

```shell
./mvnw clean package
java -jar target/crud-0.0.1-SNAPSHOT.jar
```

## Configuração

As credenciais do banco são lidas de variáveis de ambiente, com valores padrão que batem com o `compose.yaml`:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/product` | URL JDBC do banco |
| `DB_USERNAME` | `postgres` | Usuário |
| `DB_PASSWORD` | `postgres` | Senha |

Exemplo usando outro banco:

```shell
DB_URL=jdbc:postgresql://meu-servidor:5432/loja DB_USERNAME=app DB_PASSWORD=segredo ./mvnw spring-boot:run
```

Outras configurações em `application.properties`:

- `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate não altera o banco, só confere se a entidade bate com a tabela criada pelo Flyway. Se divergirem, a aplicação não sobe, o que evita erros silenciosos.
- `spring.jpa.open-in-view=false`: a conexão com o banco não fica aberta durante a renderização da resposta.
- `spring.jackson.property-naming-strategy=SNAKE_CASE`: o JSON usa `snake_case` (`price_in_cents`) enquanto o código Java usa `camelCase` (`priceInCents`).

## Endpoints

URL base: `http://localhost:8080/product`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `GET` | `/product` | Lista todos os produtos | `200 OK` |
| `GET` | `/product/{id}` | Busca um produto pelo ID | `200 OK` |
| `POST` | `/product` | Cadastra um produto | `201 Created` |
| `PUT` | `/product/{id}` | Atualiza nome e preço de um produto | `200 OK` |
| `DELETE` | `/product/{id}` | Remove um produto | `204 No Content` |

### Modelo do produto

```json
{
  "id": "3f1c2a9e-8b7d-4c1e-9a2b-1d2e3f4a5b6c",
  "name": "Teclado mecânico",
  "price_in_cents": 24990
}
```

- `id`: UUID gerado pela aplicação; não deve ser enviado no corpo.
- `name`: obrigatório, até 255 caracteres.
- `price_in_cents`: obrigatório, inteiro maior ou igual a zero. O preço é guardado em centavos para evitar erros de arredondamento com números decimais (R$ 249,90 vira `24990`).

### Exemplos com `curl`

Cadastrar:

```shell
curl -i -X POST http://localhost:8080/product \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado mecânico", "price_in_cents": 24990}'
```

A resposta traz o produto criado no corpo e o endereço dele no cabeçalho `Location`:

```
HTTP/1.1 201 Created
Location: http://localhost:8080/product/3f1c2a9e-8b7d-4c1e-9a2b-1d2e3f4a5b6c
```

Listar todos:

```shell
curl http://localhost:8080/product
```

Buscar por ID:

```shell
curl http://localhost:8080/product/3f1c2a9e-8b7d-4c1e-9a2b-1d2e3f4a5b6c
```

Atualizar:

```shell
curl -X PUT http://localhost:8080/product/3f1c2a9e-8b7d-4c1e-9a2b-1d2e3f4a5b6c \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado mecânico RGB", "price_in_cents": 29990}'
```

Remover:

```shell
curl -i -X DELETE http://localhost:8080/product/3f1c2a9e-8b7d-4c1e-9a2b-1d2e3f4a5b6c
```

## Tratamento de erros

Os erros seguem o formato Problem Details (`Content-Type: application/problem+json`).

**Produto não encontrado** (`404 Not Found`):

```json
{
  "type": "about:blank",
  "title": "Product not found",
  "status": 404,
  "detail": "Product with id 'abc' was not found",
  "instance": "/product/abc"
}
```

**Corpo inválido** (`400 Bad Request`), com o erro de cada campo:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "Request body is invalid",
  "instance": "/product",
  "errors": {
    "name": "must not be blank",
    "price_in_cents": "must be greater than or equal to 0"
  }
}
```

Outros erros do Spring MVC, como JSON malformado ou método HTTP não suportado, também são devolvidos nesse formato.

## Banco de dados e migrations

A tabela é criada pelo Flyway a partir de `src/main/resources/db/migration/V1__create-table-product.sql`:

```sql
CREATE TABLE product (
    id TEXT PRIMARY KEY UNIQUE NOT NULL,
    name TEXT NOT NULL,
    price_in_cents INT NOT NULL
);
```

O Flyway registra na tabela `flyway_schema_history` quais scripts já rodaram. Para mudar o esquema, **não edite um script que já foi aplicado**: crie um novo arquivo com a próxima versão, por exemplo `V2__add-description-to-product.sql`. Na próxima inicialização ele é aplicado automaticamente.

## Testes

```shell
./mvnw test
```

| Classe | Tipo | O que cobre |
|---|---|---|
| `ProductTest` | Unitário | Construtor, atualização e `equals`/`hashCode` da entidade |
| `ProductServiceTest` | Unitário (Mockito) | Regras do service, incluindo produto inexistente |
| `ProductControllerTests` | Controller (`@WebMvcTest`) | Rotas, status HTTP, JSON e validações, com o service simulado |
| `ProductIntegrationTest` | Integração (Testcontainers) | Fluxo completo de CRUD contra um PostgreSQL real em Docker, incluindo as migrations do Flyway |

O teste de integração precisa do Docker rodando. Sem Docker ele é ignorado automaticamente e os demais testes rodam normalmente.
