# PagerDuty Event Adapter for JENNIFER

This adapter sends JENNIFER EVENT notifications to PagerDuty using the [Events API v2](https://developer.pagerduty.com/docs/events-api-v2/overview/). It maps JENNIFER events to PagerDuty alerts, allowing for effective incident management.

## Features

- **Events API v2 Support**: Uses the latest PagerDuty API.
- **Severity Mapping**: Automatically maps JENNIFER event levels (FATAL, WARNING, etc.) to PagerDuty severities (Critical, Warning, Info).
- **Auto-Resolution**: Automatically resolves PagerDuty incidents when JENNIFER sends `NORMAL` or `RECOVERY` events.
- **Deep Linking**: Provides direct links to JENNIFER X-View transaction analysis for faster debugging.
- **Rich Metadata**: Includes detailed context like Domain, Instance, Service Name, and Metrics in the alert payload.

## Configuration

**Class Name**: `com.aries.pagerduty.PagerDutyAdapter`
**Adapter ID**: `pagerduty`

After installing the adapter `jar` file into your JENNIFER View Server, configure the following properties in the Adapter settings menu.

| Key | Required | Description | Example |
|-----|:--------:|-------------|---------|
| `integration_key` | **YES** | The Integration Key (Routing Key) from PagerDuty. | `53a54a1178494f0bc0a590b961b3297a` |
| `jennifer_url` | NO | The URL of your JENNIFER View Server. Used for links. | `https://jennifer.example.com` |

### Property Details

#### `integration_key` (Required)
This is the **Routing Key** for your PagerDuty Service. To obtain this key:

1. Log in to your PagerDuty account.
2. Navigate to **Services** > **Service Directory**.
3. Select the service where you want to receive alerts (or create a new one).
4. Click on the **Integrations** tab.
5. Click **Add a new integration**.
6. Select **Events API V2** (or "PagerDuty" if using the default) and click **Add**.
7. Copy the **Integration Key** shown in the list.

#### `jennifer_url` (Optional)
The public URL of your JENNIFER View Server.
- If configured, the adapter will add a **"Transaction Analysis"** link to the PagerDuty alert.
- Clicking this link opens the **X-View Analysis** popup for the specific transaction ID and time associated with the event.
- **Format**: `protocol://domain:port` (e.g., `http://10.0.0.1:7900` or `https://jennifer.mycompany.com`)

## Usage

Once configured, the adapter works automatically:
1. **Trigger**: When a `FATAL`, `CRITICAL`, or `WARNING` event occurs in JENNIFER, a new Incident is triggered in PagerDuty.
2. **Resolve**: When the event status returns to `NORMAL` (or `RECOVERY`, `CLEAR`), the corresponding PagerDuty Incident is automatically resolved.

## Build

To build the project from source:

```bash
mvn clean package
```

The output jar file will be located in the `dist/` directory (e.g., `dist/pagerduty-adapter-1.0.0.jar`).
