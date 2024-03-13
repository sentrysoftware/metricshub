keywords: develop, connector
description: The connector section defines the connector’s identifying information such as the display name, the version or detection criteria of the connector.

# Connector

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

The first part of a connector is a list of information defining its identity, what is the purpose of this connector and what type of platforms and systems it can be used against. The goal of this part is for someone to understand quickly what this connector is about.

## Format

```yaml
connector:
  displayName: # <string>
  platforms: # <string>
  reliesOn: # <string>
  version:  # <string>
  projectVersion: # <string> | default: ${project.version}
  information: # <string>
  detection: # <object>
```

## Properties

| Property              | Description              |
| --------------------- | ------------------------ |
| `displayName` | Name of the connector, which can be displayed in the console or reported in the metric’s attribute.(`connector.status{name="Dell OpenManage (WMI)"}`)<br />This preferably refers to the underlying instrumentation layer (e.g.: Dell OpenManage (WMI)).<br />If several connectors are required to cover different aspects of a platform (one connector for the CPU, memory, another for the disks, and a last one for the network cards, for example), the name will specify it with a dash separator.<br /><br />The typical display name therefore looks like:<br />`<Instrumentation Layer> [ - Subcomponent ] [ - OS ] [ (protocol) ]`. |
| `information` | Describes what the connector monitors and how.<br /><br /> This ends up in the documentation of the Hardware Connector Library as the description of the connector.<br /> Do not hesitate to provide details about the specific requirements for the connector to work properly. |
| `platforms` | Typical targeted system.<br /> Examples: "`HP ProLiant`" or "`Any system with SNMP`"<br /> This property is leveraged to build the Supported Platforms in the documentation.<br /> The platform name must be short and simple enough to group several connectors targeting the same type of systems.<br /> Several platforms can be specified in a comma-separated list.<br /> Connectors that monitor components that may be present in large number of platforms (e.g.: the <br /> connector which monitors network cards in all Windows systems) must specify: `Any system with [xxx]`. |
| `reliesOn` | Name of the instrumentation layer this connector leverages. This can also be considered as the <br /> technical prerequisites for this connector work, but it can only mention one instrumentation layer. <br /><br />This also ends up in the documentation and in the [Supported Platforms](../platform-requirements.html) and it is important that all<br /> connectors have a consistent wording for this property. |
| `projectVersion` | The current version of the connector library project. |
| `version` | The current version of the connector. |
| `detection` | Defines all the information required to perform connector’s detection. See specification in [Detection](detection.md). |
