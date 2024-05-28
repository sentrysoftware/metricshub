keywords: develop, connector, detection
description: This page defines the connector’s detection section.

# Detection

<div class="alert alert-warning"><span class="fa-solid fa-person-digging"></span> Documentation under construction...</div>

The detection's goal is to see if the connector will be of use, given the specified system type and the protocol enabled in your configuration.

## Format

```yaml
connector:
  # ...
  detection:
    connectionTypes: # <enum-array> | possible values: [ remote, local ] | default: local
    disableAutoDetection: # <boolean> | default: false
    onLastResort: # <string>
    appliesTo: # <enum-array> | possible values: [ vms, osf1, hp, rs6000, linux, oob, nt, network, storage, solaris, sunos ]
    supersedes: # <string-array>
    criteria: # <criteria-object-array>
    - #...
```

## Properties

| Property              | Description       |
| --------------------- | ----------------- |
| `connectionTypes` | The types of the connection `local` and/or `remote`:<br /><ul><li> `remote` the connector can be used to monitor a remote system</li><li> `local` the connector can be used to monitor the system the agent is running on.</li></ul><br />If the connector cannot be used locally (because the targeted system is something which you cannot install an agent on) use only `remote`. |
| `appliesTo` | Comma-separated list of OSes the connector can be used on.<br />Possible values:<br /><ul><li> HP = HP-UX</li><li> NT = Microsoft Windows</li><li> Linux = Linux</li><li> OSF1 = HP Tru64</li><li> Solaris = Sun Solaris</li><li> SunOS = Sun Solaris</li><li> SOLARIS = Sun Solaris</li><li> RS6000 = IBM AIX</li><li> Storage = Storage Device</li><li> VMS = HP OpenVMS</li><li> OOB = Out-of-band, management cards, etc.</li></ul> |
| `supersedes` | Comma-separated list of connectors that are superseded by this connector. In automatic detection, this connector will prevail on the listed connectors if they happen to be detected too. |
| `disableAutoDetection` | When set to true prevent the connector from running a detection. |
| `onLastResort` | Specifies that the connector is to be used as “last resort” only. The connector may be applied to monitor the system if no other connectors discovering the specified device type matches the system.<br />Example:<br />`onLastResort: enclosure`<br /> The connector will be activated if and only if no other connector matches and has an `$monitors.enclosure.discovery.mapping.source` or `$monitors.enclosure.simple.mapping.source`. |
| `criteria` | Array of criterion objects that the engine executes to decide whether the connector should be staged to monitor the host or not. These criteria are also executed by the engine if the user selects specific connectors. See specification in the [Criteria Section](criteria.md). |

## Criteria

In order for a connector to match a system, some criterion must be met. They are defined in the [Detection Section](detection.md) of the connector and checked during the discovery. If all criterion are met, the connector matches and will be used to monitor the system. If a criteria is not met, MetricsHub stops processing the detection and as far as the target system is concerned, the connector will not be used.

A maximum number of 99 detection criterion may be defined in a connector.
