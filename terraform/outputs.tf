output "apprunner_service_url" {
  description = "Public HTTPS URL of the deployed MCP server."
  value       = aws_apprunner_service.space_mcp.service_url
}

output "apprunner_service_arn" {
  description = "ARN of the App Runner service."
  value       = aws_apprunner_service.space_mcp.arn
}
