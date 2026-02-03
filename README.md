# PagerDuty Event Adapter for JENNIFER

Adapter to send Event notifications to PagerDuty.

## Configuration

| Key | Required | Description |
|-----|----------|-------------|
| `integration_key` | **YES** | PagerDuty Integration Key (Routing Key) |
| `jennifer_url` | NO | JENNIFER View Server URL for transaction links |

## Build

```bash
mvn clean package
```
