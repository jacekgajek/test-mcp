variable "aws_region" {
  description = "AWS region for deployment."
  type        = string
  default     = "us-east-1"
}

variable "service_name" {
  description = "App Runner service name."
  type        = string
  default     = "space-missions-mcp"
}

variable "container_image_identifier" {
  description = "Public ECR image URI, for example public.ecr.aws/abc123/space-mcp:latest."
  type        = string
}

variable "container_port" {
  description = "Container port exposed by the application."
  type        = number
  default     = 3000
}
