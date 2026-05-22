# test-mcp

Test MCP server in Kotlin using Ktor and the MCP Kotlin SDK.

## Features

- No authentication required
- MCP Streamable HTTP endpoint at `/mcp`
- Tool: `space.missions`
  - Input: `query` (string)
  - Returns static (intentionally fictional) responses for these mission queries:
    - Halcyon 3
    - Drake-7
    - Aurora Station
    - Cassiopeia Array

## Run locally

```bash
./gradlew :app:run
```

The server listens on:

- `http://localhost:3000/mcp` by default
- `PORT` environment variable can override the port

## Run tests

```bash
./gradlew :app:test
```

## Build container image

```bash
docker build -t space-mcp:latest .
```

## Deploy on AWS (Terraform + App Runner)

Terraform files are in `/terraform`.

### 1) Push image to Amazon ECR Public

Create an ECR Public repository and push your image (example image URI):

`public.ecr.aws/<your-alias>/space-mcp:latest`

### 2) Configure Terraform variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your ECR Public image URI.

### 3) Deploy

```bash
terraform init
terraform apply
```

After apply, Terraform outputs `apprunner_service_url`.

Your MCP endpoint will be:

`https://<apprunner_service_url>/mcp`
