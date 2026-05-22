terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_apprunner_service" "space_mcp" {
  service_name = var.service_name

  source_configuration {
    auto_deployments_enabled = false

    image_repository {
      image_repository_type = "ECR_PUBLIC"
      image_identifier      = var.container_image_identifier

      image_configuration {
        port = tostring(var.container_port)

        runtime_environment_variables = {
          PORT = tostring(var.container_port)
        }
      }
    }
  }
}
